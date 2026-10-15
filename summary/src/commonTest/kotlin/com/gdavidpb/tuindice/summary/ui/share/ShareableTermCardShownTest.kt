package com.gdavidpb.tuindice.summary.ui.share

import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareTermArtwork
import com.gdavidpb.tuindice.summary.testing.shareCardItem
import com.gdavidpb.tuindice.summary.testing.unreachableShareBackground
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The rule of the card on screen: the term and the artwork change together, and a term that comes while the artwork
// the student chose is still loading waits for it. The rule is a pure function, so the loading is fixed here.
class ShareableTermCardShownTest {
	@Test
	fun when_theTermChangesAndTheArtworkIsSettled_then_theTermShowsAtOnce() {
		val bundled = ShareBackground.included.toShareTermArtwork()
		val shown = ShareableTermCardShown.initial(term = shareCardItem(index = "3.6875"), chosen = bundled)

		val changed = shown.showingTerm(term = shareCardItem(index = "2.5000"), chosen = bundled)

		assertEquals("2.5000", changed.item.index)
		assertEquals(bundled, changed.artwork)
	}

	@Test
	fun when_theTermChangesWhileTheChosenArtworkLoads_then_theCardKeepsItsTermUntilTheArtworkIsReady() {
		val bundled = ShareBackground.included.toShareTermArtwork()
		val remote = unreachableShareBackground.toShareTermArtwork()
		val shown = ShareableTermCardShown.initial(term = shareCardItem(index = "3.6875"), chosen = bundled)

		// The student chose a remote artwork, and its picture is still loading when the term changes.
		val waiting = shown
			.showingTerm(term = shareCardItem(index = "2.5000"), chosen = remote)

		assertEquals("3.6875", waiting.item.index)
		assertEquals(bundled, waiting.artwork)
	}

	@Test
	fun when_theArtworkIsReady_then_itComesWithTheTermOfThatMoment() {
		val bundled = ShareBackground.included.toShareTermArtwork()
		val remote = unreachableShareBackground.toShareTermArtwork()
		val waiting = ShareableTermCardShown
			.initial(term = shareCardItem(index = "3.6875"), chosen = bundled)
			.showingTerm(term = shareCardItem(index = "2.5000"), chosen = remote)

		val ready = waiting.showingArtwork(
			chosen = remote,
			resolved = remote,
			term = shareCardItem(index = "2.5000")
		)

		assertEquals("2.5000", ready.item.index)
		assertEquals(remote, ready.artwork)
	}

	@Test
	fun when_thereIsNoArtworkYet_then_theTermShowsAtOnce() {
		val remote = unreachableShareBackground.toShareTermArtwork()
		val shown = ShareableTermCardShown.initial(term = shareCardItem(index = "3.6875"), chosen = remote)

		val changed = shown.showingTerm(term = shareCardItem(index = "2.5000"), chosen = remote)

		assertEquals("2.5000", changed.item.index)
		assertNull(changed.artwork)
	}

	@Test
	fun when_theChosenArtworkCannotBeLoaded_then_theCardShowsTheArtworkItFallsBackTo() {
		val remote = unreachableShareBackground.toShareTermArtwork()
		val shown = ShareableTermCardShown.initial(term = shareCardItem(), chosen = remote)

		val fallen = shown.showingArtwork(
			chosen = remote,
			resolved = remote.shownAfter(loaded = false),
			term = shareCardItem()
		)

		assertEquals(ShareBackground.included.toShareTermArtwork(), fallen.artwork)
	}

	@Test
	fun when_theArtworkOnScreenFailsToLoad_then_theCardTurnsToItsFallback() {
		val remote = unreachableShareBackground.toShareTermArtwork()
		val shown = ShareableTermCardShown(item = shareCardItem(), artwork = remote, settledFor = remote)

		val turned = shown.fallingBackFrom(failed = remote)

		assertEquals(ShareBackground.included.toShareTermArtwork(), turned.artwork)
	}

	@Test
	fun when_anotherArtworkFailsToLoad_then_theCardKeepsWhatItShows() {
		val bundled = ShareBackground.included.toShareTermArtwork()
		val remote = unreachableShareBackground.toShareTermArtwork()
		val shown = ShareableTermCardShown.initial(term = shareCardItem(), chosen = bundled)

		assertEquals(shown, shown.fallingBackFrom(failed = remote))
	}
}
