package com.gdavidpb.tuindice.assistant.domain.usecase.validator

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantAppealIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantAppealError
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal

class AppealAssistantRestrictionParamsValidator : ParamsValidator<String> {
	override fun validate(params: String) {
		val text = params.trim()

		require(text.isNotEmpty()) {
			throw AssistantAppealIllegalArgumentException(AssistantAppealError.Empty)
		}

		require(text.length <= AbuseAppeal.MAX_LENGTH) {
			throw AssistantAppealIllegalArgumentException(AssistantAppealError.TooLong)
		}
	}
}
