package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck
import com.gdavidpb.tuindice.record.data.mutation.RecordMutationSyncSpec
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.data.contract.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.contract.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.contract.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.source.database.mapper.toQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterAdd
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuarterDataSource(
	private val localDataSource: QuarterLocalDataSource,
	private val remoteDataSource: QuarterRemoteDataSource,
	private val settingsDataSource: QuarterSettingsDataSource,
	private val mutationEngine: StoreBackedMutationEngine<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, RecordMutationAck>,
	private val identifierRepository: IdentifierRepository
) : QuarterRepository {
	private val mutationSyncSpec = RecordMutationSyncSpec(
		localDataSource = localDataSource,
		remoteDataSource = remoteDataSource,
		refreshRemoteSnapshot = ::refreshRemoteSnapshot
	)

	override suspend fun observeQuartersFlow(): Flow<List<Quarter>> {
		return localDataSource.getQuartersFlow()
			.map { localQuarters -> localQuarters.map { it.toQuarter() } }
	}

	override suspend fun updateQuarters() {
		val isOnCooldown = settingsDataSource.isGetQuartersOnCooldown()

		if (!isOnCooldown) {
			val snapshotVersion = mutationEngine.currentMutationVersion()
			val remoteQuarters = remoteDataSource.getQuarters()
			if (snapshotVersion == mutationEngine.currentMutationVersion()) {
				localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
				settingsDataSource.setGetQuartersOnCooldown()
			}
		}

		mutationEngine.drain(
			scopeKey = RECORD_MUTATION_SCOPE,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = false
		)
	}

	override suspend fun addQuarter(add: QuarterAdd) {
		val confirmedQuarters = localDataSource.getConfirmedQuarters()
		val mutation = buildPendingAddQuarterMutation(
			add = add,
			expectedRevision = confirmedQuarters.maxOfOrNull { quarter -> quarter.revision } ?: 0L
		)
		val mutationVersion = mutationEngine.beginMutation(replaceKey = mutation.replaceKey)
		mutationEngine.rememberMutationVersion(
			mutationId = mutation.mutationId,
			version = mutationVersion
		)

		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		val quarter = localDataSource.getQuarter(remove.id)
			?: return
		if (!QuarterMutationPolicy.canDelete(isCurrent = quarter.isCurrent, isReadOnly = quarter.isReadOnly)) return

		mutationEngine.beginMutation()
		val mutation = buildPendingRemoveQuarterMutation(
			quarterId = remove.id,
			expectedRevision = quarter.revision
		)

		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		val quarter = localDataSource.getQuarter(set.quarterId)
			?: return
		if (!QuarterMutationPolicy.canEditGrades(isReadOnly = quarter.isReadOnly)) return

		val mutationVersion = if (set.commit) {
			mutationEngine.beginMutation(replaceKey = "subject:${set.id}")
		} else {
			null
		}

		val localResult = localDataSource.setSubjectGradeAndRecompute(
			qid = set.quarterId,
			sid = set.id,
			grade = set.grade,
			commit = set.commit
		)

		if (!set.commit) return
		val appliedResult = localResult as? SetSubjectGradeResult.Applied ?: return
		val expectedRevision = appliedResult.expectedRevision
		val existing = mutationEngine.getPendingMutations(RECORD_MUTATION_SCOPE)
			.firstOrNull { mutation -> mutation.replaceKey == "subject:${set.id}" }

		if (appliedResult.updatedQuarters.isEmpty() && existing == null) return

		val mutation = buildPendingSetSubjectGradeMutation(
			quarterId = set.quarterId,
			subjectId = set.id,
			grade = set.grade,
			expectedRevision = expectedRevision
		)

		if (mutationVersion != null) {
			mutationEngine.rememberMutationVersion(
				mutationId = mutation.mutationId,
				version = mutationVersion
			)
		}

		mutationEngine.submit(
			mutation = mutation,
			syncSpec = mutationSyncSpec,
			propagateTerminalErrors = true
		)
	}

	private suspend fun refreshRemoteSnapshot(): List<RemoteQuarter> {
		val snapshotVersion = mutationEngine.currentMutationVersion()
		val remoteQuarters = remoteDataSource.getQuarters()
		if (snapshotVersion == mutationEngine.currentMutationVersion()) {
			localDataSource.saveQuarters(remoteQuarters.map { quarter -> quarter.toLocalQuarter() })
		}
		return remoteQuarters
	}

	private fun buildPendingSetSubjectGradeMutation(
		quarterId: String,
		subjectId: String,
		grade: Int,
		expectedRevision: Long
	): MutationEnvelope<String, RecordMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = RecordMutation.SetSubjectGrade(
				quarterId = quarterId,
				subjectId = subjectId,
				grade = grade
			),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingAddQuarterMutation(
		add: QuarterAdd,
		expectedRevision: Long
	): MutationEnvelope<String, RecordMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = RecordMutation.AddQuarter(
				quarter = add.quarter,
				year = add.year,
				subjects = add.subjects.map { subject ->
					RecordMutation.AddQuarter.SubjectSeed(
						code = subject.code,
						grade = subject.grade
					)
				}
			),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}

	private fun buildPendingRemoveQuarterMutation(
		quarterId: String,
		expectedRevision: Long
	): MutationEnvelope<String, RecordMutation> {
		val now = currentTimeMillis()
		return MutationEnvelope(
			mutationId = identifierRepository.generateRandomIdentifier(),
			scopeKey = RECORD_MUTATION_SCOPE,
			command = RecordMutation.RemoveQuarter(quarterId = quarterId),
			precondition = MutationPrecondition.Revision(expectedRevision),
			status = PendingMutationStatus.Pending,
			createdAt = now,
			updatedAt = now,
			lastError = null
		)
	}
}
