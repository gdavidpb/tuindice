package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where the evaluations of an [AddEvaluationsArgs] came from. */
@Serializable
public enum class EvaluationDraftSource {
	@SerialName("manual")
	MANUAL,

	@SerialName("photo")
	PHOTO
}
