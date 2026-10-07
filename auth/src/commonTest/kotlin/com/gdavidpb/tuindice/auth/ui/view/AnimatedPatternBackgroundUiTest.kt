package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AnimatedPatternBackgroundUiTest {
	@Test
	fun when_backgroundRendered_then_displaysCanvasNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AnimatedPatternBackground()
		}

		assertNodeVisible(AuthUiTags.AnimatedPatternBackground)
	}

	@Test
	fun when_customAnimationParametersProvided_then_backgroundStillRenders() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AnimatedPatternBackground(
				tileScale = 0.8f,
				alpha = 0.9f,
				durationMillis = 5_000
			)
		}

		assertNodeVisible(AuthUiTags.AnimatedPatternBackground)
	}

	@Test
	fun when_animationsAreDisabled_then_thePatternDoesNotMove() = runTuIndiceUiTest {
		var progress = 0f

		// Same clock mode as the enabled case, so only the flag differs between the two.
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			val progressState by patternProgress(
				animationsEnabled = false,
				durationMillis = 10_000
			)

			progress = progressState
		}

		advanceAnimationsBy(15_000)

		assertEquals(0f, progress, "the pattern must stay at its first position")
	}

	@Test
	fun when_animationsAreEnabled_then_thePatternMoves() = runTuIndiceUiTest {
		var progress = 0f

		// The harness cancels infinite animations that start while the clock auto-advances.
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			val progressState by patternProgress(
				animationsEnabled = true,
				durationMillis = 10_000
			)

			progress = progressState
		}

		advanceAnimationsBy(2_500)

		assertTrue(progress > 0f, "the pattern should have advanced, progress was $progress")
	}

	@Test
	fun when_theCompositionDisablesAnimations_then_theBackgroundDoesNotMove() = runTuIndiceUiTest {
		var progress = 0f

		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				val progressState by currentPatternProgress(durationMillis = 10_000)

				progress = progressState
			}
		}

		advanceAnimationsBy(15_000)

		assertEquals(0f, progress, "the background reads the flag of the composition")
	}

	@Test
	fun when_theCompositionEnablesAnimations_then_theBackgroundMoves() = runTuIndiceUiTest {
		var progress = 0f

		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides true) {
				val progressState by currentPatternProgress(durationMillis = 10_000)

				progress = progressState
			}
		}

		advanceAnimationsBy(2_500)

		assertTrue(progress > 0f, "the background should have advanced, progress was $progress")
	}
}
