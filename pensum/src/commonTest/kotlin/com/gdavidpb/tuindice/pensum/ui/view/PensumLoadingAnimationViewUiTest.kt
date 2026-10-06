package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumLoadingAnimationViewUiTest {
	@Test
	fun when_themeIsLight_then_animationOccupiesItsDeclaredSquare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				PensumLoadingAnimationView()
			}
		}

		assertNodeVisible(PensumUiTags.LoadingAnimation)
		onNodeWithTag(PensumUiTags.LoadingAnimation)
			.assertWidthIsEqualTo(240.dp)
			.assertHeightIsEqualTo(240.dp)
	}

	@Test
	fun when_themeIsDark_then_darkAnimationOccupiesTheSameSquare() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				PensumLoadingAnimationView()
			}
		}

		assertNodeVisible(PensumUiTags.LoadingAnimation)
		onNodeWithTag(PensumUiTags.LoadingAnimation)
			.assertWidthIsEqualTo(240.dp)
			.assertHeightIsEqualTo(240.dp)
	}
}
