package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmCreatePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeletePlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditPlanIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmProjectionIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs
import kotlinx.coroutines.flow.Flow

// The confirmation of the intents about the plans and the projection of the record.
class AssistantChatPlanIntents(
	private val confirmCreatePlanIntentUseCase: ConfirmCreatePlanIntentUseCase,
	private val confirmEditPlanIntentUseCase: ConfirmEditPlanIntentUseCase,
	private val confirmDeletePlanIntentUseCase: ConfirmDeletePlanIntentUseCase,
	private val confirmProjectionIntentUseCase: ConfirmProjectionIntentUseCase
) {
	// The confirmation of an intent of these kinds, or null when the kind is another one.
	fun confirmation(
		args: IntentArgs,
		params: ConfirmIntentParams
	): Flow<UseCaseState<OutcomeKind, AssistantIntentError>>? {
		return when (args) {
			is CreatePlanArgs -> confirmCreatePlanIntentUseCase.execute(params)
			is EditPlanArgs -> confirmEditPlanIntentUseCase.execute(params)
			is RemovePlanArgs -> confirmDeletePlanIntentUseCase.execute(params)
			is ProjectionArgs -> confirmProjectionIntentUseCase.execute(params)
			else -> null
		}
	}
}
