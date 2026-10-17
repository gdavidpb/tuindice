package com.gdavidpb.tuindice.chatcontract.extraction

import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import kotlinx.serialization.Serializable

/**
 * A photo of an evaluation plan sent to `POST /assistant/v1/extractions`, outside the chat thread. [imageBase64] is the
 * image the app already scaled down (at most [ExtractionLimits.MAX_IMAGE_EDGE_PX] on its longest edge), in one of
 * [ExtractionLimits.MEDIA_TYPES]. The service never stores the image. [clientRequestId] makes a retry answer the same
 * proposal, and [conversationId] is the chat the proposal's result is reported to, when the photo came from one.
 */
@Serializable
public data class ExtractionRequest(
	public val clientRequestId: String,
	public val mediaType: String,
	public val imageBase64: String,
	public val screenContext: ScreenContext? = null,
	public val conversationId: String? = null
)
