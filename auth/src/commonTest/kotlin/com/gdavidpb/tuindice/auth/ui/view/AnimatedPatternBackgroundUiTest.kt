package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
}
