package com.gdavidpb.tuindice.summary.domain.model

/**
 * An artwork a term card can wear: [title] and [author] are its credit, and [shortName] is what the picker calls it.
 * [author] is null when the work has no author to credit.
 */
data class ShareBackground(
	val id: String,
	val title: String,
	val author: String?,
	val shortName: String,
	val art: ShareBackgroundArt
) {
	companion object {
		const val INCLUDED_ID = "laberinto-cromovegetal"

		// The background the app ships with. It is the fallback when there is no catalog, and always the first one.
		val included = ShareBackground(
			id = INCLUDED_ID,
			title = "Laberinto Cromovegetal",
			author = "Carlos Cruz-Diez",
			shortName = "Laberinto",
			art = ShareBackgroundArt.Bundled
		)
	}
}
