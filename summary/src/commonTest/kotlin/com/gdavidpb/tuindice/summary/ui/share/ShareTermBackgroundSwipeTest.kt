package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The swipe across the card moves to the backgrounds beside the chosen one, and the sheet's state resolves them.
class ShareTermBackgroundSwipeTest {
	@Test
	fun when_theMiddleOneIsChosen_then_aSwipeLeadsToEitherSide() {
		val content = content(chosen = 1)

		assertEquals("c", content.nextBackgroundId)
		assertEquals("a", content.previousBackgroundId)
	}

	@Test
	fun when_anEndIsChosen_then_aSwipePastItLeadsNowhere() {
		assertNull(content(chosen = 0).previousBackgroundId)
		assertNull(content(chosen = 2).nextBackgroundId)
	}

	@Test
	fun when_nothingIsChosen_then_aSwipeLeadsNowhere() {
		val content = content(chosen = -1)

		assertNull(content.previousBackgroundId)
		assertNull(content.nextBackgroundId)
	}

	@Test
	fun when_theChoiceMoves_then_theCardAndItsNeighboursFollowIt() {
		val content = content(chosen = 0)
		val moved = content.copy(
			backgrounds = content.backgrounds.map { item -> item.copy(selected = item.artwork.id == "b") }
		)

		assertEquals("b", moved.selectedBackground?.id)
		assertEquals("a", moved.previousBackgroundId)
		assertEquals("c", moved.nextBackgroundId)
	}

	private fun content(chosen: Int): ShareTerm.State.Content {
		return ShareTerm.State.Content(
			terms = emptyList(),
			backgrounds = listOf("a", "b", "c").mapIndexed { index, id ->
				ShareTermBackgroundItem(
					artwork = ShareTermArtwork(
						id = id,
						shortName = id,
						title = id,
						author = null,
						art = ShareTermArt.Bundled
					),
					selected = index == chosen
				)
			}
		)
	}
}
