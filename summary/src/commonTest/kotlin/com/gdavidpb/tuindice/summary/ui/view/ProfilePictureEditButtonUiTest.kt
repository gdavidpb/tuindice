package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfilePictureEditButtonUiTest {
	@Test
	fun when_enabledAndIdle_then_announcesTheEditActionAndInvokesClick() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			ProfilePictureEditButton(
				isEnabled = true,
				isUploading = false,
				onClick = { clicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)
		assertNodeEnabled(SummaryUiTags.ProfilePictureEditButton)
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton)
			.assertHasClickAction()
			.assertContentDescriptionEquals("Editar foto de perfil")
		assertNodeHidden(
			tag = SummaryUiTags.ProfilePictureLoadingIndicator,
			useUnmergedTree = true
		)

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, clicks)
	}

	@Test
	fun when_uploading_then_showsTheLoaderInsteadOfTheEditIcon() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureEditButton(
				isEnabled = false,
				isUploading = true,
				onClick = {}
			)
		}

		assertNodeVisible(
			tag = SummaryUiTags.ProfilePictureLoadingIndicator,
			useUnmergedTree = true
		)
		// The icon is what carries the label: with the loader in its place nothing announces "edit".
		onNodeWithContentDescription("Editar foto de perfil").assertDoesNotExist()
	}

	@Test
	fun when_disabled_then_ignoresClicks() = runTuIndiceUiTest {
		var clicks = 0

		setTuIndiceTestContent {
			ProfilePictureEditButton(
				isEnabled = false,
				isUploading = false,
				onClick = { clicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)
		assertNodeDisabled(SummaryUiTags.ProfilePictureEditButton)

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()
		waitForIdle()

		assertEquals(0, clicks)
	}

	@Test
	fun when_laidOut_then_keepsItsTouchTargetWhateverItShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProfilePictureEditButton(
				isEnabled = true,
				isUploading = true,
				onClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton)
			.assertWidthIsEqualTo(48.dp)
			.assertHeightIsEqualTo(48.dp)
	}
}
