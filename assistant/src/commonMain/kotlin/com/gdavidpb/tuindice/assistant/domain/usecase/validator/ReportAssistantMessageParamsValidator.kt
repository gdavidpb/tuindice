package com.gdavidpb.tuindice.assistant.domain.usecase.validator

import com.gdavidpb.tuindice.assistant.domain.exception.ReportAssistantMessageIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ReportAssistantMessageParams
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator

class ReportAssistantMessageParamsValidator : ParamsValidator<ReportAssistantMessageParams> {
	override fun validate(params: ReportAssistantMessageParams) {
		val comment = params.comment?.trim().orEmpty()

		require(comment.length <= AssistantInputLimits.REPORT_COMMENT_MAX_LENGTH) {
			throw ReportAssistantMessageIllegalArgumentException(ReportAssistantMessageError.CommentTooLong)
		}
	}
}
