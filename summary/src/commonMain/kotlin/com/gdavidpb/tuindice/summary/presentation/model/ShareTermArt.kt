package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/**
 * What a card draws behind its figures: [Bundled] is the artwork the app ships with, and [Remote] is fetched from the
 * catalog as its full card ([Remote.cardUrl]) and as its thumbnail ([Remote.thumbUrl]).
 */
sealed interface ShareTermArt {
	data object Bundled : ShareTermArt

	@Immutable
	data class Remote(
		val cardUrl: String,
		val thumbUrl: String
	) : ShareTermArt
}
