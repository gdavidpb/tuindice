package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShareTermBackgroundSwipeTest {
	@Test
	fun when_theMiddleOneIsChosen_then_aSwipeLeadsToEitherSide() {
		val strip = strip(chosen = 1)

		assertEquals("c", neighbourBackgroundId(backgrounds = strip, towardsNext = true))
		assertEquals("a", neighbourBackgroundId(backgrounds = strip, towardsNext = false))
	}

	@Test
	fun when_anEndIsChosen_then_aSwipePastItLeadsNowhere() {
		assertNull(neighbourBackgroundId(backgrounds = strip(chosen = 0), towardsNext = false))
		assertNull(neighbourBackgroundId(backgrounds = strip(chosen = 2), towardsNext = true))
	}

	@Test
	fun when_nothingIsChosen_then_aSwipeLeadsNowhere() {
		assertNull(neighbourBackgroundId(backgrounds = strip(chosen = -1), towardsNext = true))
	}

	private fun strip(chosen: Int): List<ShareTermBackgroundItem> {
		return listOf("a", "b", "c").mapIndexed { index, id ->
			ShareTermBackgroundItem(
				background = ShareBackground(
					id = id,
					title = id,
					author = null,
					shortName = id,
					art = ShareBackgroundArt.Bundled
				),
				selected = index == chosen
			)
		}
	}
}
