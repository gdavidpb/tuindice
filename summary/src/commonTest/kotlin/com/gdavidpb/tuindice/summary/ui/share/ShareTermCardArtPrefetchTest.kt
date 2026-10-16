package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.testing.bibliotecaArtwork
import kotlin.test.Test
import kotlin.test.assertEquals

// The order the sheet brings the card pictures in: nearest to the chosen background first, never the chosen one, never
// the one the app ships with.
class ShareTermCardArtPrefetchTest {
	@Test
	fun when_aBackgroundIsChosen_then_theOthersComeNearestFirst() {
		val backgrounds = listOf("a", "b", "c", "d", "e").backgrounds(chosen = "c")

		assertEquals(
			listOf(cardUrl("b"), cardUrl("d"), cardUrl("a"), cardUrl("e")),
			backgrounds.cardUrlsToPrefetch()
		)
	}

	@Test
	fun when_theFirstBackgroundIsChosen_then_theOthersComeInTheirOrder() {
		val backgrounds = listOf("a", "b", "c").backgrounds(chosen = "a")

		assertEquals(listOf(cardUrl("b"), cardUrl("c")), backgrounds.cardUrlsToPrefetch())
	}

	@Test
	fun when_theBundledBackgroundIsInTheStrip_then_itIsNotBrought() {
		val backgrounds = listOf(ShareTermBackgroundItem(artwork = bibliotecaArtwork(), selected = false)) +
			listOf("a", "b").backgrounds(chosen = "a")

		assertEquals(listOf(cardUrl("b")), backgrounds.cardUrlsToPrefetch())
	}

	private fun List<String>.backgrounds(chosen: String): List<ShareTermBackgroundItem> {
		return map { id -> ShareTermBackgroundItem(artwork = remoteArtwork(id), selected = id == chosen) }
	}

	private fun remoteArtwork(id: String) = ShareTermArtwork(
		id = id,
		shortName = id,
		title = id,
		author = null,
		art = ShareTermArt.Remote(cardUrl = cardUrl(id), thumbUrl = "https://example.test/$id/thumb.webp")
	)

	private fun cardUrl(id: String) = "https://example.test/$id/card.webp"
}
