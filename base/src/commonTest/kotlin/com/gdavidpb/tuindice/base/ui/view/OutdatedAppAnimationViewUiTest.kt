package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class OutdatedAppAnimationViewUiTest {
	@Test
	fun when_outdatedAppAnimationRendered_then_displaysAnimationNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			OutdatedAppAnimationView()
		}

		assertNodeVisible(BaseUiTags.OutdatedAppAnimation)
	}

	@Test
	fun when_outdatedAppAnimationRenderedWithHighDensity_then_displaysAnimationNode() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 2f) {
			OutdatedAppAnimationView()
		}

		assertNodeVisible(BaseUiTags.OutdatedAppAnimation)
	}
}
