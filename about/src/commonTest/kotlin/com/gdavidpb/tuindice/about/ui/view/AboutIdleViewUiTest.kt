package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AboutIdleViewUiTest {
	@Test
	fun when_idleViewRendered_then_hidesInteractiveItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutIdleView()
		}

		assertNodeHidden(AboutUiTags.OpenCreativeCommons)
	}

	@Test
	fun when_idleViewRendered_then_hidesSecondaryAboutActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AboutIdleView()
		}

		assertNodeHidden(AboutUiTags.OpenTerms)
		assertNodeHidden(AboutUiTags.ReportBug)
	}
}
