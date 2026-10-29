package com.gdavidpb.tuindice.assistant.domain.exception

import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError

class ReportAssistantMessageIllegalArgumentException(
	val error: ReportAssistantMessageError
) : IllegalArgumentException()
