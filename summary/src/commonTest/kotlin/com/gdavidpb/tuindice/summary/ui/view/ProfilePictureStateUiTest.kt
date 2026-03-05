package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfilePictureStateUiTest {
	@Test
	fun when_pictureUrlChanges_then_rememberedStateIsUpdated() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			var profilePictureUrl by remember {
				mutableStateOf("https://tuindice.test/initial.jpg")
			}
			val profilePictureState = rememberProfilePictureState(url = profilePictureUrl)

			Column {
				Text(text = profilePictureState.value.url)
				Text(text = profilePictureState.value.isLoading.toString())
				Text(
					text = "Cambiar",
					modifier = Modifier
						.clickable {
							profilePictureUrl = "https://tuindice.test/updated.jpg"
						}
				)
			}
		}

		onNodeWithText("https://tuindice.test/initial.jpg").assertIsDisplayed()
		onNodeWithText("false").assertIsDisplayed()

		onNodeWithText("Cambiar").performClick()

		onNodeWithText("https://tuindice.test/updated.jpg").assertIsDisplayed()
	}

	@Test
	fun when_loadingFlagChanges_then_rememberedStateUpdatesLoadingValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			var isLoading by remember {
				mutableStateOf(false)
			}
			val profilePictureState = rememberProfilePictureState(
				url = "https://tuindice.test/avatar.jpg",
				isLoading = isLoading
			)

			Column {
				Text(text = profilePictureState.value.isLoading.toString())
				Text(
					text = "Toggle loading",
					modifier = Modifier.clickable {
						isLoading = !isLoading
					}
				)
			}
		}

		onNodeWithText("false").assertIsDisplayed()

		onNodeWithText("Toggle loading").performClick()

		onNodeWithText("true").assertIsDisplayed()
	}
}
