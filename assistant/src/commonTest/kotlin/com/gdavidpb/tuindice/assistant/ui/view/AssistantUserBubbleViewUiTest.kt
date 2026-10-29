package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantUserBubbleViewUiTest {
	@Test
	fun when_studentMessageIsShown_then_alignsRightWithinEightyPercentOfTheWidth() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(CONTAINER_WIDTH_DP.dp).testTag(CONTAINER_TAG)) {
				AssistantUserBubbleView(item = ChatItem.User(id = "user-1", text = "Hola", isFailed = false))
			}
		}

		assertNodeVisible(AssistantUiTags.Message.bubble("user-1"))
		val container = onNodeWithTag(CONTAINER_TAG).fetchSemanticsNode().boundsInRoot
		val bubble = onNodeWithTag(AssistantUiTags.Message.bubble("user-1")).fetchSemanticsNode().boundsInRoot

		assertTrue(bubble.width <= container.width * MAX_WIDTH_FRACTION)
		assertTrue(abs(container.right - bubble.right) < ALIGNMENT_TOLERANCE_DP)
	}

	@Test
	fun when_studentMessageIsLong_then_stopsAtEightyPercentOfTheWidthAndStaysAtTheEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(CONTAINER_WIDTH_DP.dp).testTag(CONTAINER_TAG)) {
				AssistantUserBubbleView(item = ChatItem.User(id = "user-3", text = LONG_TEXT, isFailed = false))
			}
		}

		assertNodeVisible(AssistantUiTags.Message.bubble("user-3"))
		val container = onNodeWithTag(CONTAINER_TAG).fetchSemanticsNode().boundsInRoot
		val bubble = onNodeWithTag(AssistantUiTags.Message.bubble("user-3")).fetchSemanticsNode().boundsInRoot

		assertTrue(bubble.width <= container.width * MAX_WIDTH_FRACTION, "the bubble is ${bubble.width}px wide")
		assertTrue(abs(container.right - bubble.right) < ALIGNMENT_TOLERANCE_DP)
	}

	@Test
	fun when_turnFailed_then_showsItsErrorLineUnderTheBubble() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantUserBubbleView(item = ChatItem.User(id = "user-2", text = "Hola", isFailed = true))
		}

		assertNodeVisible(AssistantUiTags.Message.bubbleError("user-2"))
		onNodeWithText(FAILED_TEXT).assertIsDisplayed()
	}

	private companion object {
		const val CONTAINER_TAG = "assistant_user_bubble_test_container"
		const val CONTAINER_WIDTH_DP = 320
		const val MAX_WIDTH_FRACTION = 0.8f
		const val ALIGNMENT_TOLERANCE_DP = 1f
		const val FAILED_TEXT = "No pudimos enviar tu mensaje."
		const val LONG_TEXT = "Quisiera saber si puedo cambiar la sección del laboratorio de Física Dos por otra que no " +
			"choque con mi trabajo de tiempo completo durante el próximo semestre, por favor."
	}
}
