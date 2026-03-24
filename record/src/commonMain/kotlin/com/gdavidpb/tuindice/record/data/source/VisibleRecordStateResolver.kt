package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.record.data.model.QuarterSyncResolution
import com.gdavidpb.tuindice.record.data.model.SubjectGradePreview
import com.gdavidpb.tuindice.record.data.model.SubjectPreviewKey
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.domain.policy.toQuarterMutationType
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine

class VisibleRecordStateResolver(
	private val indexComputationEngine: IndexComputationEngine
) {
	fun resolveVisibleState(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<PendingMutation<RecordMutation>>,
		gradePreviewSnapshot: Map<SubjectPreviewKey, SubjectGradePreview>
	): List<LocalQuarter> {
		val withoutDeletedQuarters = confirmedSnapshot.filterNot { quarter ->
			quarter.id in pendingDeletedQuarterIds(
				confirmedSnapshot = confirmedSnapshot,
				pendingMutations = pendingMutations
			)
		}
		val snapshotWithPendingMutations = applyPendingSubjectMutationsToSnapshot(
			confirmedSnapshot = withoutDeletedQuarters,
			pendingMutations = pendingMutations
		)

		return applyPreviewGradesToSnapshot(
			snapshot = snapshotWithPendingMutations,
			gradePreviewSnapshot = gradePreviewSnapshot
		)
	}

	fun resolveIncomingSnapshot(
		incomingQuarters: List<LocalQuarter>,
		pendingMutations: List<PendingMutation<RecordMutation>>
	): QuarterSyncResolution {
		val incomingQuartersById = incomingQuarters.associateBy { quarter -> quarter.id }
		val replacedClosedQuarterIds = incomingQuarters
			.asSequence()
			.filter { quarter -> quarter.isReadOnly }
			.mapTo(linkedSetOf()) { quarter -> quarter.id }
		val invalidatedQuarterIds = linkedSetOf<String>()
		val invalidatedMutationIds = pendingMutations
			.mapNotNullTo(linkedSetOf()) { mutation ->
				when (val payload = mutation.mutation) {
					is RecordMutation.AddQuarter -> null

					is RecordMutation.SetSubjectGrade -> {
						val quarter = incomingQuartersById[payload.quarterId]
						when {
							quarter == null -> {
								invalidatedQuarterIds += payload.quarterId
								mutation.mutationId
							}

							!QuarterMutationPolicy.canApplyPendingMutation(
								isCurrent = quarter.isCurrent,
								isReadOnly = quarter.isReadOnly,
								mutationType = payload.toQuarterMutationType()
							) -> {
								invalidatedQuarterIds += payload.quarterId
								mutation.mutationId
							}

							else -> null
						}
					}

					is RecordMutation.RemoveQuarter -> {
						val quarter = incomingQuartersById[payload.quarterId]
						when {
							quarter == null -> {
								invalidatedQuarterIds += payload.quarterId
								mutation.mutationId
							}

							!QuarterMutationPolicy.canApplyPendingMutation(
								isCurrent = quarter.isCurrent,
								isReadOnly = quarter.isReadOnly,
								mutationType = payload.toQuarterMutationType()
							) -> {
								invalidatedQuarterIds += payload.quarterId
								mutation.mutationId
							}

							else -> null
						}
					}
				}
			}

		return QuarterSyncResolution(
			replacedClosedQuarterIds = replacedClosedQuarterIds,
			invalidatedQuarterIds = invalidatedQuarterIds,
			invalidatedMutationIds = invalidatedMutationIds,
			compatiblePendingMutations = pendingMutations.filterNot { mutation ->
				mutation.mutationId in invalidatedMutationIds
			}
		)
	}

	private fun applyPendingSubjectMutationsToSnapshot(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<PendingMutation<RecordMutation>>
	): List<LocalQuarter> {
		val pendingGradesBySubject = pendingSubjectGrades(
			confirmedSnapshot = confirmedSnapshot,
			pendingMutations = pendingMutations
		)

		if (pendingGradesBySubject.isEmpty()) return confirmedSnapshot

		var affectedStartDate = Long.MAX_VALUE
		var hasChanges = false

		val patchedSnapshot = confirmedSnapshot.map { quarter ->
			if (!QuarterMutationPolicy.canEditGrades(isReadOnly = quarter.isReadOnly)) return@map quarter

			var quarterChanged = false

			val patchedSubjects = quarter.subjects.map { subject ->
				val pendingGrade = pendingGradesBySubject[subject.id]
					?: return@map subject

				if (pendingGrade == subject.grade) return@map subject

				hasChanges = true
				quarterChanged = true
				affectedStartDate = minOf(affectedStartDate, quarter.startDate)

				subject.copy(grade = pendingGrade)
			}

			if (quarterChanged)
				quarter.copy(subjects = patchedSubjects)
			else
				quarter
		}

		if (!hasChanges) return confirmedSnapshot

		return indexComputationEngine.recompute(
			quarters = patchedSnapshot,
			affectedStartDate = affectedStartDate
		).quarters.toCanonicalOrder()
	}

	private fun applyPreviewGradesToSnapshot(
		snapshot: List<LocalQuarter>,
		gradePreviewSnapshot: Map<SubjectPreviewKey, SubjectGradePreview>
	): List<LocalQuarter> {
		if (gradePreviewSnapshot.isEmpty()) return snapshot

		var affectedStartDate = Long.MAX_VALUE
		var hasChanges = false

		val patchedSnapshot = snapshot.map { quarter ->
			if (!QuarterMutationPolicy.canEditGrades(isReadOnly = quarter.isReadOnly)) return@map quarter

			var quarterChanged = false

			val patchedSubjects = quarter.subjects.map { subject ->
				val preview = gradePreviewSnapshot[SubjectPreviewKey(quarter.id, subject.id)]
					?: return@map subject

				if (preview.requestedGrade == subject.grade) return@map subject

				hasChanges = true
				quarterChanged = true
				affectedStartDate = minOf(affectedStartDate, quarter.startDate)

				subject.copy(grade = preview.requestedGrade)
			}

			if (quarterChanged)
				quarter.copy(subjects = patchedSubjects)
			else
				quarter
		}

		if (!hasChanges) return snapshot

		return indexComputationEngine.recompute(
			quarters = patchedSnapshot,
			affectedStartDate = affectedStartDate
		).quarters.toCanonicalOrder()
	}

	private fun pendingDeletedQuarterIds(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<PendingMutation<RecordMutation>>
	): Set<String> {
		val quartersById = confirmedSnapshot.associateBy { quarter -> quarter.id }

		return pendingMutations.mapNotNullTo(hashSetOf()) { mutation ->
			val payload = mutation.mutation as? RecordMutation.RemoveQuarter
				?: return@mapNotNullTo null
			val quarter = quartersById[payload.quarterId]
				?: return@mapNotNullTo null

			payload.quarterId.takeIf {
				QuarterMutationPolicy.canApplyPendingMutation(
					isCurrent = quarter.isCurrent,
					isReadOnly = quarter.isReadOnly,
					mutationType = payload.toQuarterMutationType()
				)
			}
		}
	}

	private fun pendingSubjectGrades(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<PendingMutation<RecordMutation>>
	): Map<String, Int> {
		val quartersById = confirmedSnapshot.associateBy { quarter -> quarter.id }

		return pendingMutations.mapNotNull { mutation ->
			val payload = mutation.mutation as? RecordMutation.SetSubjectGrade
				?: return@mapNotNull null
			val quarter = quartersById[payload.quarterId]
				?: return@mapNotNull null

			if (
				!QuarterMutationPolicy.canApplyPendingMutation(
					isCurrent = quarter.isCurrent,
					isReadOnly = quarter.isReadOnly,
					mutationType = payload.toQuarterMutationType()
				)
			) {
				return@mapNotNull null
			}

			payload.subjectId to payload.grade
		}.toMap()
	}

	private fun List<LocalQuarter>.toCanonicalOrder(): List<LocalQuarter> {
		return sortedWith(
			compareByDescending<LocalQuarter> { quarter -> quarter.startDate }
				.thenBy { quarter -> quarter.id }
		)
	}
}
