package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
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

		// An edit dropped here aims at an attempt the confirmed record does not have, so the record does not
		// contain its change: discardMutation publishes its result as CANCELLED at once, so the assistant intent
		// that follows the edit is settled now. A deletion counts the same way, since the record confirms nothing
		// for an attempt it does not have. A Pending or Failed edit that expects a newer revision than this record
		// is held back rather than dropped, and nothing is published for it: it was built on a record this one
		// predates, so the attempt may simply not be in it yet, and it waits for a record at that revision. A
		// FailedTerminal edit is never held back: the server rejected it for good, so this record judges it whatever
		// revision it expects, and nothing is published for it again, since its result was published when it failed.
		mutations
			.filter { mutation ->
				val target = mutation.command.targetAttemptId()
				val awaitsNewerRecord = mutation.status != PendingMutationStatus.FailedTerminal &&
					(mutation.expectedRevision ?: Long.MIN_VALUE) > record.revision

				target != null && target !in knownAttemptIds && !awaitsNewerRecord
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
