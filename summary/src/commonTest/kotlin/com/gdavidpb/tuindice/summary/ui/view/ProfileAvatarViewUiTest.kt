package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfileAvatarViewUiTest {
	@Test
	fun when_theStudentHasNoPicture_then_showsOnlyTheNeutralPersonIcon() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileAvatarView(pictureUrl = "", pictureVersion = 0, size = 32.dp)
		}

		assertNodeVisible(SummaryUiTags.ProfileAvatarFallback, useUnmergedTree = true)
		assertNodeHidden(SummaryUiTags.ProfileAvatarImage, useUnmergedTree = true)
	}

	@Test
	fun when_theStudentHasAPicture_then_theImageIsComposedOverTheFallback() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfileAvatarView(
				pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
				pictureVersion = 2,
				size = 32.dp
			)
		}

		assertNodeVisible(SummaryUiTags.ProfileAvatarFallback, useUnmergedTree = true)
		assertNodeVisible(SummaryUiTags.ProfileAvatarImage, useUnmergedTree = true)
	}
}
