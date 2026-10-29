package com.gdavidpb.tuindice.assistant.domain.exception

import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantAppealError

class AssistantAppealIllegalArgumentException(
	val error: AssistantAppealError
) : IllegalArgumentException()
