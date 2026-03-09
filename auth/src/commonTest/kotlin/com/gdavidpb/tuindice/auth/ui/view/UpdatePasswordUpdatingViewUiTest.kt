package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
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

		assertNodeVisible(AuthUiTags.UpdatePasswordUpdatingIndicator)
	}

	@Test
	fun when_updatingViewRendered_then_displaysSingleProgressIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			UpdatePasswordUpdatingView()
		}

		onAllNodesWithTag(AuthUiTags.UpdatePasswordUpdatingIndicator)
			.assertCountEquals(1)
	}
}
