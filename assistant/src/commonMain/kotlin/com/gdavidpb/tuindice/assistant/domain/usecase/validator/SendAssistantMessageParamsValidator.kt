package com.gdavidpb.tuindice.assistant.domain.usecase.validator

import com.gdavidpb.tuindice.assistant.domain.exception.SendAssistantMessageIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator

class SendAssistantMessageParamsValidator : ParamsValidator<SendAssistantMessageParams> {
	override fun validate(params: SendAssistantMessageParams) {
		val text = params.text?.trim()?.takeIf { it.isNotEmpty() }

		require(text != null || params.suggestionId != null) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}

		require(text == null || text.length <= AssistantInputLimits.MESSAGE_MAX_LENGTH) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}
	}
}
