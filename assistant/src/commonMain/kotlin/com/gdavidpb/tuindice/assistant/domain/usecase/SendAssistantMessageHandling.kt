package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.SendAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.SendAssistantMessageParamsValidator

// How a send is judged and reported: the rules its params must pass and the mapping of its failures. They are one unit,
// as the use case of the send takes them together, so they are grouped to keep the send's constructor within its limit.
class SendAssistantMessageHandling(
	val paramsValidator: SendAssistantMessageParamsValidator,
	val exceptionHandler: SendAssistantMessageExceptionHandler
)
