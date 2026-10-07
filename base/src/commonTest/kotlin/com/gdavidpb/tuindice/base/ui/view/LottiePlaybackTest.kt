package com.gdavidpb.tuindice.base.ui.view

import io.github.alexzhirkevich.compottie.Compottie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LottiePlaybackTest {
	@Test
	fun when_animationsAreOff_then_isNotPlayed() {
		val playback = lottiePlayback(animationsEnabled = false, iterations = Compottie.IterateForever)

		assertFalse(playback.isPlaying)
	}

	@Test
	fun when_animationsAreOn_then_isPlayed() {
		val playback = lottiePlayback(animationsEnabled = true, iterations = Compottie.IterateForever)

		assertTrue(playback.isPlaying)
	}

	@Test
	fun when_aFiniteNumberOfIterationsIsRequested_then_itIsKeptWhateverTheSwitch() {
		assertEquals(3, lottiePlayback(animationsEnabled = true, iterations = 3).iterations)
		assertEquals(3, lottiePlayback(animationsEnabled = false, iterations = 3).iterations)
		assertEquals(
			Compottie.IterateForever,
			lottiePlayback(animationsEnabled = true, iterations = Compottie.IterateForever).iterations
		)
	}
}
