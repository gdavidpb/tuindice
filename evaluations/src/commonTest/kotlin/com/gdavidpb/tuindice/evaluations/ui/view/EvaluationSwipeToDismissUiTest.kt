package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationSwipeToDismissUiTest {
	@Test
	fun when_rendered_then_displaysDismissContainerAndContent() = runTuIndiceUiTest {
		val dismissedDirections = mutableListOf<SwipeToDismissBoxValue>()

		setTuIndiceTestContent {
			val dismissState = rememberSwipeToDismissBoxState()

			EvaluationSwipeToDismiss(
				state = dismissState,
				onDismiss = { direction ->
					dismissedDirections += direction
				}
			) {
				Text(text = "Evaluacion")
			}
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
		onNodeWithText("Evaluacion").assertIsDisplayed()
		assertTrue(dismissedDirections.isEmpty())

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeRight() }

		waitUntil(timeoutMillis = 2_000) {
			dismissedDirections.isNotEmpty()
		}

		assertEquals(SwipeToDismissBoxValue.StartToEnd, dismissedDirections.last())
	}

	@Test
	fun when_swipedLeft_then_dispatchesEndToStartDismissDirection() = runTuIndiceUiTest {
		val dismissedDirections = mutableListOf<SwipeToDismissBoxValue>()

		setTuIndiceTestContent {
			val dismissState = rememberSwipeToDismissBoxState()

			EvaluationSwipeToDismiss(
				state = dismissState,
				onDismiss = { direction ->
					dismissedDirections += direction
				}
			) {
				Text(text = "Evaluacion")
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationSwipeToDismissContainer)
			.performTouchInput { swipeLeft() }

		waitUntil(timeoutMillis = 2_000) {
			dismissedDirections.isNotEmpty()
		}

		assertEquals(SwipeToDismissBoxValue.EndToStart, dismissedDirections.last())
	}
}
