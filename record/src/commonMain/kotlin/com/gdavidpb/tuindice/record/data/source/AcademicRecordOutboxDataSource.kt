package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordOutboxDataRepository

class AcademicRecordOutboxDataSource(
	private val mutationEngine: StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>
) : AcademicRecordOutboxDataRepository {
	override suspend fun reconcileWithConfirmedRecord(record: VersionedAcademicRecord) {
		val mutations = mutationEngine.getMutations(RECORD_MUTATION_SCOPE)
		val knownAttemptIds = record.knownAttemptIds(mutations.map { mutation -> mutation.command })

		mutations
			.filter { mutation ->
				val target = mutation.command.targetAttemptId()

				target != null && target !in knownAttemptIds
			}
			.forEach { mutation ->
				mutationEngine.discardMutation(
					scopeKey = RECORD_MUTATION_SCOPE,
					mutationId = mutation.mutationId
				)
			}

		mutationEngine.advancePendingRevisions(
			scopeKey = RECORD_MUTATION_SCOPE,
			revision = record.revision
		)
	}

	// An edit may aim at a subject of a synthetic term that is itself still waiting to be sent, so
	// the subjects those queued term edits introduce count as known too.
	private fun VersionedAcademicRecord.knownAttemptIds(
		commands: List<AcademicRecordMutation>
	): Set<String> {
		val confirmed = record.terms.flatMap { term -> term.attempts.map { attempt -> attempt.id } }
		val introduced = commands.flatMap { command ->
			when (command) {
				is AcademicRecordMutation.AddSyntheticTerm -> command.attempts.map { seed -> seed.attemptId }
				is AcademicRecordMutation.UpdateSyntheticTerm -> command.attempts.map { seed -> seed.attemptId }
				else -> emptyList()
			}
		}

		return (confirmed + introduced).toSet()
	}

	private fun AcademicRecordMutation.targetAttemptId(): String? = when (this) {
		is AcademicRecordMutation.UpsertAttemptOverride -> attemptId
		is AcademicRecordMutation.DeleteAttemptOverride -> attemptId
		else -> null
	}
}
