package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * An artwork the card can wear, as the sheet and the card paint it: its [id], the [shortName] the picker calls it, its
 * credit ([title], and the [author] when the work has one), what it draws ([art]) and the [fallback] the card turns to
 * when [art] cannot be loaded: the artwork the app ships with, credit and all. The bundled artwork has no fallback.
 */
@Immutable
data class ShareTermArtwork(
	val id: String,
	val shortName: String,
	val title: String,
	val author: String?,
	val art: ShareTermArt,
	val fallback: ShareTermArtwork? = null
)
