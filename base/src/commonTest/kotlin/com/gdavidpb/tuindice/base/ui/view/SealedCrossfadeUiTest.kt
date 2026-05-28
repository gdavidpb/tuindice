package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SealedCrossfadeUiTest {
	@Test
	fun when_targetStateChanges_then_rendersNewContent() = runTuIndiceUiTest {
		val state = mutableStateOf<UiState>(UiState.Idle)

		setTuIndiceTestContent {
			SealedCrossfade(targetState = state.value) { targetState ->
				when (targetState) {
					UiState.Idle -> Text("Idle")
					UiState.Loading -> Text("Loading")
				}
			}
		}

		onNodeWithText("Idle").assertIsDisplayed()

		runOnIdle {
			state.value = UiState.Loading
		}
		advanceAnimationsBy(millis = 500)

		onNodeWithText("Loading").assertIsDisplayed()
	}

	@Test
	fun when_initialStateIsLoading_then_rendersLoadingImmediately() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SealedCrossfade(targetState = UiState.Loading) { targetState ->
				when (targetState) {
					UiState.Idle -> Text("Idle")
					UiState.Loading -> Text("Loading")
				}
			}
		}

		onNodeWithText("Loading").assertIsDisplayed()
	}

	private sealed interface UiState {
		data object Idle : UiState
		data object Loading : UiState
	}
}
