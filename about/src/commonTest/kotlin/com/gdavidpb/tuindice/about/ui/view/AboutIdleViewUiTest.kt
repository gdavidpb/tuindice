package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
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
		assertNodeHidden(AboutUiTags.ContactDeveloper)
	}

	@Test
	fun when_settingsIdleViewRendered_then_hidesTheSettingsRows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SettingsIdleView()
		}

		assertNodeHidden(SettingsUiTags.OpenTerms)
		assertNodeHidden(SettingsUiTags.ReportBug)
		assertNodeHidden(SettingsUiTags.SignOut)
	}
}
