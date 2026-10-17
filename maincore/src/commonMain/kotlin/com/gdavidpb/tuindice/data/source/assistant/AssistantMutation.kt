package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState

// One mutation of an intent: its id, and the call of its use case, which answers the Data or the Error of the call.
internal class AssistantMutation(
	val mutationId: String,
	val call: suspend () -> UseCaseState<*, *>
)
