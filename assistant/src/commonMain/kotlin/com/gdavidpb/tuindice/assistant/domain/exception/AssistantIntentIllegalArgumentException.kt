package com.gdavidpb.tuindice.assistant.domain.exception

import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError

class AssistantIntentIllegalArgumentException(
	val error: AssistantIntentError
) : IllegalArgumentException()
