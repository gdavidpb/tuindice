package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StatsLoadingAnimationViewUiTest {
	@Test
	fun when_statsLoadingAnimationRendered_then_displaysAnimationNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StatsLoadingAnimationView()
		}

		assertNodeVisible(BaseUiTags.StatsLoadingAnimation)
	}
}
