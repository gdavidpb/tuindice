package com.gdavidpb.tuindice.chatcontract.extraction

import com.gdavidpb.tuindice.chatcontract.intent.Intent
import kotlinx.serialization.Serializable

/**
 * The answer of an extraction. [intent], when the photo held evaluations, is an
 * `evaluaciones.agregar/1` proposal whose args carry `source = PHOTO`, decided and reported like
 * any other intent. [message] is the sentence the app shows with it, in Spanish, also when
 * nothing was found.
 */
@Serializable
public data class ExtractionResult(
	public val message: String,
	public val intent: Intent? = null
)
