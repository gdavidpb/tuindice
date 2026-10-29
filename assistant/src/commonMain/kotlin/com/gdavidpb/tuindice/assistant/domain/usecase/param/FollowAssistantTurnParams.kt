package com.gdavidpb.tuindice.assistant.domain.usecase.param

data class FollowAssistantTurnParams(
	val turnId: String,
	val afterSeq: Long
)
