package com.gdavidpb.tuindice.summary.data.model

import kotlinx.serialization.Serializable

// One artwork of the catalog. The author is null when the work has none to credit.
@Serializable
data class ShareBackgroundResponse(
	val id: String,
	val order: Int,
	val version: Int,
	val title: String,
	val author: String? = null,
	val shortName: String,
	val card: String,
	val thumb: String
)
