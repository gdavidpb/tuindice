package com.gdavidpb.tuindice.summary.data.model

import kotlinx.serialization.Serializable

// The catalog as the backend answers it: every background that can be shared, in no particular order.
@Serializable
data class ShareBackgroundsResponse(
	val backgrounds: List<ShareBackgroundResponse>
)
