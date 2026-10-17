package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordRevisionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordMutationOutcomesUseCase

/**
 * What the record ports share: the revision guard that runs before a mutation, and the settling of the mutation with
 * the record's outcomes. A record intent is one mutation, so it answers with that mutation's execution.
 */
class RecordIntentRunner(
	private val getRecordRevisionUseCase: GetRecordRevisionUseCase,
	private val observeRecordMutationOutcomesUseCase: ObserveRecordMutationOutcomesUseCase
) {
	// CONFLICT when the record moved on since the intent was built; FAILED when its revision cannot be read.
	// Null when the mutation may run. Without an expected revision, the intent does not check the record's.
	suspend fun revisionConflictOrNull(expectedRevision: Long?): IntentExecution? {
		if (expectedRevision == null) return null

		return when (val state = getRecordRevisionUseCase.execute(Unit).settled()) {
			is UseCaseState.Data -> revisionAnswer(movedOn = state.value != expectedRevision)
			is UseCaseState.Error -> IntentExecution(OutcomeKind.FAILED, errorDetailCode(state.error))
			UseCaseState.Loading -> IntentExecution(OutcomeKind.FAILED, DETAIL_UNHANDLED)
		}
	}

	suspend fun runMutation(mutationId: String, call: suspend () -> UseCaseState<*, *>): IntentExecution {
		return settleMutations(
			mutations = listOf(AssistantMutation(mutationId, call)),
			outcomes = observeRecordMutationOutcomesUseCase.execute(Unit).outcomes()
		).withRevisionAfterConfirmation()
	}

	private fun revisionAnswer(movedOn: Boolean): IntentExecution? {
		return if (movedOn) IntentExecution(OutcomeKind.CONFLICT) else null
	}

	// A confirmed write answers the revision the record is at after it. The other outcomes carry none.
	private suspend fun IntentExecution.withRevisionAfterConfirmation(): IntentExecution {
		if (outcome != OutcomeKind.CONFIRMED) return this

		return copy(newRevision = getRecordRevisionUseCase.currentRevisionOrNull())
	}
}

// The record revision as its read answers it, or null when the read fails.
internal suspend fun GetRecordRevisionUseCase.currentRevisionOrNull(): Long? {
	val state = execute(Unit).settled()

	return if (state is UseCaseState.Data) state.value else null
}
