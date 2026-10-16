package com.gdavidpb.tuindice.summary.domain.model

/**
 * Where the artwork of a background comes from: [Bundled] ships inside the app, and [Remote] is fetched from the
 * catalog as its full card and its thumbnail.
 */
sealed interface ShareBackgroundArt {
	data object Bundled : ShareBackgroundArt

	data class Remote(
		val cardUrl: String,
		val thumbUrl: String
	) : ShareBackgroundArt
}
