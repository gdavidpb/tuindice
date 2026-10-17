package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kind of the record an [EntityRef] points to. */
@Serializable
public enum class EntityKind {
	@SerialName("term")
	TERM,

	@SerialName("subject")
	SUBJECT,

	@SerialName("evaluation")
	EVALUATION,

	@SerialName("attempt")
	ATTEMPT,

	@SerialName("pensum")
	PENSUM,

	@SerialName("plan")
	PLAN
}
