package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.assistant.ui.contentState
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The chat's motion on the test clock. The pixel checks follow AssistantAgentMessageViewUiTest (density 1, a tagged host
// whose capture is read), and the clock is driven by hand: nothing advances unless the test says so.
@OptIn(ExperimentalTestApi::class)
class AssistantChatMotionUiTest {
	@Test
	fun when_animationsAreOff_then_aNewAnswerShowsItsFullTextAndItsActionsAtOnce() = runTuIndiceUiTest {
		val state = mutableStateOf(contentState())
		setConversation(animationsEnabled = false, state = { state.value })

		mainClock.autoAdvance = false
		state.value = contentState(items = listOf(agentMessage(id = MESSAGE_ID, text = ANSWER)))
		mainClock.advanceTimeBy(FRAME_MILLIS)

		onNodeWithText(ANSWER).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).assertIsDisplayed()
		assertNotNull(painted(AssistantUiTags.Message.copy(MESSAGE_ID)), "the actions are drawn at once")
		assertNotNull(painted(AssistantUiTags.Message.agent(MESSAGE_ID)), "the answer is drawn at once")
	}

	@Test
	fun when_animationsAreOn_then_aNewAnswerHasItsFullTextFromTheStartAndItsActionsEnterAfterTheReveal() = runTuIndiceUiTest {
		val state = mutableStateOf(contentState())
		setConversation(animationsEnabled = true, state = { state.value })

		mainClock.autoAdvance = false
		state.value = contentState(items = listOf(agentMessage(id = MESSAGE_ID, text = ANSWER)))
		mainClock.advanceTimeBy(FRAME_MILLIS)

		// The reveal is visual only: the semantics (and so accessibility and Copy) hold the whole text at once.
		onNodeWithText(ANSWER).assertExists()
		assertNull(painted(AssistantUiTags.Message.copy(MESSAGE_ID)), "the actions have not entered yet")

		mainClock.advanceTimeBy(REVEAL_AND_ENTRANCE_MILLIS)
		waitForIdle()

		onNodeWithText(ANSWER).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).assertIsDisplayed()
		assertNotNull(painted(AssistantUiTags.Message.copy(MESSAGE_ID)), "the actions have entered")
	}

	@Test
	fun when_theSheetOpensWithHistory_then_itIsDrawnAtOnceWithoutAdvancingTheClock() = runTuIndiceUiTest {
		val state = mutableStateOf(contentState(items = listOf(agentMessage(id = MESSAGE_ID, text = ANSWER))))
		mainClock.autoAdvance = false
		setConversation(animationsEnabled = true, state = { state.value })
		mainClock.advanceTimeBy(FRAME_MILLIS)

		onNodeWithText(ANSWER).assertIsDisplayed()
		assertNotNull(painted(AssistantUiTags.Message.copy(MESSAGE_ID)), "the history shows its actions at once")
		assertNotNull(painted(AssistantUiTags.Message.agent(MESSAGE_ID)), "the history shows its text at once")
	}

	@Test
	fun when_aThumbWasAlreadyChosen_then_itDoesNotPop() = runTuIndiceUiTest {
		setActions(useful = { true })

		mainClock.autoAdvance = false
		mainClock.advanceTimeBy(FRAME_MILLIS)
		val settled = assertNotNull(painted(AssistantUiTags.Message.useful(MESSAGE_ID)))
		mainClock.advanceTimeBy(POP_PEAK_MILLIS)
		val midPop = assertNotNull(painted(AssistantUiTags.Message.useful(MESSAGE_ID)))

		assertTrue(abs(settled.width - midPop.width) <= SIZE_TOLERANCE, "the width does not change: $settled / $midPop")
		assertTrue(abs(settled.height - midPop.height) <= SIZE_TOLERANCE, "the height does not change: $settled / $midPop")
	}

	@Test
	fun when_aThumbBecomesChosen_then_itPopsAndSettles() = runTuIndiceUiTest {
		val useful = mutableStateOf<Boolean?>(null)
		setActions(useful = { useful.value })

		mainClock.autoAdvance = false
		mainClock.advanceTimeBy(FRAME_MILLIS)
		useful.value = true
		mainClock.advanceTimeBy(POP_PEAK_MILLIS)
		val midPop = assertNotNull(painted(AssistantUiTags.Message.useful(MESSAGE_ID)))
		mainClock.advanceTimeBy(SETTLE_MILLIS)
		val settled = assertNotNull(painted(AssistantUiTags.Message.useful(MESSAGE_ID)))

		assertTrue(midPop.width > settled.width + SIZE_TOLERANCE, "the glyph is larger at the peak of the pop")
	}

	private fun ComposeUiTest.setConversation(animationsEnabled: Boolean, state: () -> AssistantChat.State.Content) {
		setHost(animationsEnabled) {
			AssistantChatConversationView(content = state(), onAction = {}, modifier = Modifier.fillMaxSize())
		}
	}

	private fun ComposeUiTest.setActions(useful: () -> Boolean?) {
		setHost(animationsEnabled = true) {
			AssistantMessageActionsView(messageId = MESSAGE_ID, useful = useful(), reported = false, onAction = {})
		}
	}

	// The content in a tagged host on the theme's surface, padded so that the host's corner is bare background.
	private fun ComposeUiTest.setHost(animationsEnabled: Boolean, content: @Composable () -> Unit) {
		setTuIndiceTestContent(density = PIXEL_DENSITY) {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides animationsEnabled) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.testTag(HOST_TAG)
						.background(MaterialTheme.colorScheme.surface)
						.padding(all = HOST_PADDING)
				) {
					content()
				}
			}
		}
	}

	private class Painted(val width: Int, val height: Int) {
		override fun toString(): String = "${width}x$height"
	}

	// The size of the box that holds every pixel inside [tag] that differs from the host's background, or null when the
	// node is blank. The capture and the bounds are both in pixels, from the host's top-left corner.
	private fun ComposeUiTest.painted(tag: String): Painted? {
		waitForIdle()
		val host = onNodeWithTag(HOST_TAG).fetchSemanticsNode().boundsInRoot
		val node = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
		val pixels = onNodeWithTag(HOST_TAG).captureToImage().toPixelMap()
		val background = pixels[0, 0]

		val left = (node.left - host.left).toInt().coerceIn(0, pixels.width)
		val right = (node.right - host.left).toInt().coerceIn(0, pixels.width)
		val top = (node.top - host.top).toInt().coerceIn(0, pixels.height)
		val bottom = (node.bottom - host.top).toInt().coerceIn(0, pixels.height)

		var minX = Int.MAX_VALUE
		var maxX = -1
		var minY = Int.MAX_VALUE
		var maxY = -1
		for (x in left until right) {
			for (y in top until bottom) {
				if (!pixels[x, y].isCloseTo(background)) {
					minX = minOf(minX, x)
					maxX = maxOf(maxX, x)
					minY = minOf(minY, y)
					maxY = maxOf(maxY, y)
				}
			}
		}
		return if (maxX < 0) null else Painted(width = maxX - minX + 1, height = maxY - minY + 1)
	}

	private fun Color.isCloseTo(other: Color): Boolean =
		abs(red - other.red) <= COLOR_TOLERANCE &&
			abs(green - other.green) <= COLOR_TOLERANCE &&
			abs(blue - other.blue) <= COLOR_TOLERANCE

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val ANSWER = "Tu promedio es 16,2 este trimestre."
		const val HOST_TAG = "assistant_motion_pixel_host"

		// Density 1, as the other pixel tests: a denser content root does not fit the capture surface on the simulator.
		const val PIXEL_DENSITY = 1f
		val HOST_PADDING = 8.dp
		const val COLOR_TOLERANCE = 0.05f
		const val SIZE_TOLERANCE = 1

		const val FRAME_MILLIS = 32L

		// Past the reveal of a short answer (about 210 ms), the stagger and the 300 ms entrance of its actions.
		const val REVEAL_AND_ENTRANCE_MILLIS = 1_500L

		// Half of the 260 ms pop: the icon is at its largest.
		const val POP_PEAK_MILLIS = 130L
		const val SETTLE_MILLIS = 600L
	}
}
