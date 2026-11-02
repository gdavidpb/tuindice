package com.gdavidpb.tuindice.assistant.domain.usecase.validator

import com.gdavidpb.tuindice.assistant.domain.exception.SendAssistantMessageIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits

class SendAssistantMessageParamsValidator : ParamsValidator<SendAssistantMessageParams> {
	override fun validate(params: SendAssistantMessageParams) {
		val text = params.text?.trim()?.takeIf { it.isNotEmpty() }

		// A message is text, a suggested reply, or attachments, with or without text.
		require(text != null || params.suggestionId != null || params.attachments.isNotEmpty()) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}

		require(text == null || text.length <= AssistantInputLimits.MESSAGE_MAX_LENGTH) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}

		require(params.attachments.size <= AttachmentLimits.MAX_PER_MESSAGE) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}

		require(params.attachments.all { attachment -> attachment.state is AssistantAttachmentState.Ready }) {
			throw SendAssistantMessageIllegalArgumentException(SendAssistantMessageError.InvalidMessage)
		}
	}
}
