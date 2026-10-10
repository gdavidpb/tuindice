package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body of an overlay 409. The server omits what it does not have, so every field has a default. */
@Serializable
data class OverlayConflictResponse(
	val message: String? = null,
	val reason: String? = null,
	@SerialName("current_revision") val currentRevision: Long? = null
)
