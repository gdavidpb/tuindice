package com.gdavidpb.tuindice.record.data.resolver

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationProjectionSpec
import com.gdavidpb.tuindice.record.data.model.QuarterSyncResolution
import com.gdavidpb.tuindice.record.data.model.SubjectGradePreview
import com.gdavidpb.tuindice.record.data.model.SubjectPreviewKey
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.domain.policy.toQuarterMutationType
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
import com.gdavidpb.tuindice.record.domain.service.SimulationProjectionEngine

class VisibleRecordStateResolver(
	private val indexComputationEngine: IndexComputationEngine,
	private val simulationProjectionEngine: SimulationProjectionEngine
) : MutationProjectionSpec<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, QuarterSyncResolution> {
	private data class PendingSubjectMutation(
		val grade: Int?,
		val status: SubjectStatus?
	)

	override fun projectVisibleState(
		confirmedState: List<LocalQuarter>,
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>
	): List<LocalQuarter> {
		val deletedQuarterIds = pendingDeletedQuarterIds(
			confirmedSnapshot = confirmedState,
			pendingMutations = pendingMutations
		)
		val withoutDeletedQuarters = confirmedState.filterNot { quarter ->
			quarter.id in deletedQuarterIds
		}
		val deletedAffectedStartDate = confirmedState
			.filter { quarter -> quarter.id in deletedQuarterIds }
			.minOfOrNull(LocalQuarter::startDate)
		val snapshotWithPendingMutations = applyPendingSubjectMutationsToSnapshot(
			confirmedSnapshot = withoutDeletedQuarters,
			pendingMutations = pendingMutations
		)

		return if (deletedAffectedStartDate != null) {
			recomputeProjectedSnapshot(
				snapshot = snapshotWithPendingMutations,
				affectedStartDate = deletedAffectedStartDate
			)
		} else {
			snapshotWithPendingMutations
		}
	}

	fun resolveVisibleState(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>,
		gradePreviewSnapshot: Map<SubjectPreviewKey, SubjectGradePreview>
	): List<LocalQuarter> {
		val snapshotWithPendingMutations = projectVisibleState(
			confirmedState = confirmedSnapshot,
			pendingMutations = pendingMutations
		)
		return applyPreviewGradesToSnapshot(
			snapshot = snapshotWithPendingMutations,
			gradePreviewSnapshot = gradePreviewSnapshot
		)
	}

	override fun resolveIncomingState(
		incomingConfirmedState: List<LocalQuarter>,
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>
	): QuarterSyncResolution {
		val incomingQuartersById = incomingConfirmedState.associateBy { quarter -> quarter.id }
		val replacedClosedQuarterIds = incomingConfirmedState
			.asSequence()
			.filter { quarter -> quarter.isReadOnly }
			.mapTo(linkedSetOf()) { quarter -> quarter.id }
		val invalidatedQuarterIds = linkedSetOf<String>()
		val invalidatedMutationIds = pendingMutations
			.mapNotNullTo(linkedSetOf()) { mutation ->
				when (val payload = mutation.command) {
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
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>
	): List<LocalQuarter> {
		val pendingMutationsBySubject = pendingSubjectMutations(
			confirmedSnapshot = confirmedSnapshot,
			pendingMutations = pendingMutations
		)

		if (pendingMutationsBySubject.isEmpty()) return confirmedSnapshot

		var affectedStartDate = Long.MAX_VALUE
		var hasChanges = false

		val patchedSnapshot = confirmedSnapshot.map { quarter ->
			if (!QuarterMutationPolicy.canEditGrades(isReadOnly = quarter.isReadOnly)) return@map quarter

			var quarterChanged = false

			val patchedSubjects = quarter.subjects.map { subject ->
				val pendingMutation = pendingMutationsBySubject[subject.id]
					?: return@map subject

				val nextGrade = pendingMutation.grade ?: subject.grade
				val nextStatus = pendingMutation.status ?: subject.status

				if ((nextGrade == subject.grade) && (nextStatus == subject.status)) return@map subject

				hasChanges = true
				quarterChanged = true
				affectedStartDate = minOf(affectedStartDate, quarter.startDate)

				subject.copy(
					grade = nextGrade,
					status = nextStatus
				)
			}

			if (quarterChanged)
				quarter.copy(subjects = patchedSubjects)
			else
				quarter
		}

		if (!hasChanges) return confirmedSnapshot

		return recomputeProjectedSnapshot(
			snapshot = patchedSnapshot,
			affectedStartDate = affectedStartDate
		)
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

		return recomputeProjectedSnapshot(
			snapshot = patchedSnapshot,
			affectedStartDate = affectedStartDate
		)
	}

	private fun recomputeProjectedSnapshot(
		snapshot: List<LocalQuarter>,
		affectedStartDate: Long
	): List<LocalQuarter> {
		val recomputedOfficial = indexComputationEngine.recompute(
			quarters = snapshot,
			affectedStartDate = affectedStartDate
		).quarters.toCanonicalOrder()

		return simulationProjectionEngine.recompute(recomputedOfficial)
			.toCanonicalOrder()
	}

	private fun pendingDeletedQuarterIds(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>
	): Set<String> {
		val quartersById = confirmedSnapshot.associateBy { quarter -> quarter.id }

		return pendingMutations.mapNotNullTo(hashSetOf()) { mutation ->
			val payload = mutation.command as? RecordMutation.RemoveQuarter
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

	private fun pendingSubjectMutations(
		confirmedSnapshot: List<LocalQuarter>,
		pendingMutations: List<MutationEnvelope<String, RecordMutation>>
	): Map<String, PendingSubjectMutation> {
		val quartersById = confirmedSnapshot.associateBy { quarter -> quarter.id }

		return pendingMutations.mapNotNull { mutation ->
			val payload = mutation.command as? RecordMutation.SetSubjectGrade
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

			payload.subjectId to PendingSubjectMutation(
				grade = payload.grade,
				status = payload.status?.let { value ->
					SubjectStatus.entries.firstOrNull { status -> status.value == value }
				}
			)
		}.toMap()
	}

	private fun List<LocalQuarter>.toCanonicalOrder(): List<LocalQuarter> {
		return sortedWith(
			compareByDescending<LocalQuarter> { quarter -> quarter.startDate }
				.thenBy { quarter -> quarter.id }
		)
	}
}
