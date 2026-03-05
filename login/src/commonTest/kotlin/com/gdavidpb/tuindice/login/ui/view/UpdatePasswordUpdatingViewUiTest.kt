package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.login.ui.LoginUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class UpdatePasswordUpdatingViewUiTest {
	@Test
	fun when_updatingViewRendered_then_displaysProgressIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordUpdatingView()
		}

		assertNodeVisible(LoginUiTags.UpdatePasswordUpdatingIndicator)
	}

	@Test
	fun when_updatingViewRendered_then_displaysSingleProgressIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordUpdatingView()
		}

		onAllNodesWithTag(LoginUiTags.UpdatePasswordUpdatingIndicator)
			.assertCountEquals(1)
	}
}
