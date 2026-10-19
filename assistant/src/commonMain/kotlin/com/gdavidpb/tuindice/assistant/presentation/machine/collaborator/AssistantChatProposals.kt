package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.flow.Flow

// The change proposals the assistant makes, in two groups: the student's records and the enrollment proof
// ([intents]), and the plans and the projection ([planIntents]). The chat confirms either group the same way, so the
// group that owns the kind of an intent answers its confirmation here.
class AssistantChatProposals(
	val intents: AssistantChatIntents,
	val planIntents: AssistantChatPlanIntents
) {
	// The confirmation of an intent with [args]; null when no group owns its kind.
	fun confirmation(
		args: IntentArgs,
		params: ConfirmIntentParams
	): Flow<UseCaseState<OutcomeKind, AssistantIntentError>>? {
		return intents.confirmation(args, params) ?: planIntents.confirmation(args, params)
	}
}
