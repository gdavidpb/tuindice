package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EmptyStateAnimationViewUiTest {
	@Test
	fun when_emptyStateAnimationRendered_then_displaysAnimationNode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EmptyStateAnimationView()
		}

		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
	}
}
