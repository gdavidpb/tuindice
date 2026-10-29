package com.gdavidpb.tuindice.assistant.domain.exception

import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError

class SendAssistantMessageIllegalArgumentException(
	val error: SendAssistantMessageError
) : IllegalArgumentException()
