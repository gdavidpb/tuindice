package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.unreachableShareBackground
import kotlin.test.Test
import kotlin.test.assertEquals

// The fallback rule of the card: one remote artwork that cannot be loaded gives way to the one the app ships with.
class ShareableTermCardArtworkTest {
	@Test
	fun when_aRemoteArtworkFailsToLoad_then_theCardShowsTheIncludedOneWithItsCredit() {
		val remote = unreachableShareBackground.toShareTermArtwork()

		val shown = remote.shownAfter(loaded = false)

		assertEquals(ShareBackground.included.toShareTermArtwork(), shown)
	}

	@Test
	fun when_aRemoteArtworkLoads_then_theCardShowsItself() {
		val remote = unreachableShareBackground.toShareTermArtwork()

		assertEquals(remote, remote.shownAfter(loaded = true))
	}
}
