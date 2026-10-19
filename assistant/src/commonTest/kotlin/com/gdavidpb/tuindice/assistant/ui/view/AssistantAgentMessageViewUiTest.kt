package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantAgentMessageViewUiTest {
	@Test
	fun when_copyAndThumbsAreTapped_then_eachDispatchesItsAction() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Message.copy(MESSAGE_ID))
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).performClick()
		onNodeWithTag(AssistantUiTags.Message.useful(MESSAGE_ID)).performClick()
		onNodeWithTag(AssistantUiTags.Message.notUseful(MESSAGE_ID)).performClick()

		assertIs<AssistantChat.Action.CopyMessage>(actions[0])
		assertTrue(assertIs<AssistantChat.Action.RateMessage>(actions[1]).useful)
		assertFalse(assertIs<AssistantChat.Action.RateMessage>(actions[2]).useful)
	}

	@Test
	fun when_useful_then_thatThumbIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID, useful = true), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Message.useful(MESSAGE_ID))
		onNodeWithTag(AssistantUiTags.Message.useful(MESSAGE_ID)).assertIsSelected()
		onNodeWithTag(AssistantUiTags.Message.notUseful(MESSAGE_ID)).assertIsNotSelected()
	}

	@Test
	fun when_reportReasonIsChosen_then_reportsTheMessageWithoutComment() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = { actions.add(it) })
		}

		onNodeWithTag(AssistantUiTags.Message.report(MESSAGE_ID)).performClick()
		assertNodeVisible(AssistantUiTags.Message.reportReason(MESSAGE_ID, AssistantReportReason.HARMFUL))
		onNodeWithTag(AssistantUiTags.Message.reportReason(MESSAGE_ID, AssistantReportReason.HARMFUL)).performClick()

		val report = assertIs<AssistantChat.Action.ReportMessage>(actions.single())
		assertEquals(AssistantReportReason.HARMFUL, report.reason)
		assertNull(report.comment)
	}

	@Test
	fun when_theActionsAreShown_then_eachIconIs18DpOnAButtonOf36Dp_withNoSpaceBetweenThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = {})
		}

		val tags = listOf(
			AssistantUiTags.Message.copy(MESSAGE_ID),
			AssistantUiTags.Message.useful(MESSAGE_ID),
			AssistantUiTags.Message.notUseful(MESSAGE_ID),
			AssistantUiTags.Message.report(MESSAGE_ID)
		)
		tags.forEach { tag ->
			onNodeWithTag(tag).assertWidthIsEqualTo(36.dp).assertHeightIsEqualTo(36.dp)
		}
		ACTION_DESCRIPTIONS.forEach { description ->
			onNodeWithContentDescription(description, useUnmergedTree = true)
				.assertWidthIsEqualTo(18.dp)
				.assertHeightIsEqualTo(18.dp)
		}
		val bounds = tags.map { tag -> onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot }
		bounds.zipWithNext().forEach { (previous, next) ->
			assertEquals(previous.right, next.left, absoluteTolerance = PIXEL_TOLERANCE)
		}
	}

	@Test
	fun when_aTouchLandsJustOutsideAnAction_then_itStillReachesIt() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			Column(modifier = Modifier.padding(all = 24.dp)) {
				AssistantMessageActionsView(messageId = MESSAGE_ID, useful = null, onAction = { actions.add(it) })
			}
		}

		// 5 dp above the copy button: inside its 48 dp touch area, outside the 36 dp it draws.
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).performTouchInput {
			click(Offset(x = centerX, y = -5.dp.toPx()))
		}

		assertIs<AssistantChat.Action.CopyMessage>(actions.single())
	}

	@Test
	fun when_useful_then_theChosenThumbIsFilledWithTheInverseSurface_inLightTheme() = runTuIndiceUiTest {
		val scheme = lightColorScheme()
		setMessageInTheme(scheme, agentMessage(id = MESSAGE_ID, useful = true))

		assertGlyphDrawnIn(AssistantUiTags.Message.useful(MESSAGE_ID), scheme.inverseSurface)
		assertGlyphNotDrawnIn(AssistantUiTags.Message.useful(MESSAGE_ID), scheme.primary)
	}

	@Test
	fun when_useful_then_theChosenThumbIsFilledWithTheInverseSurface_inDarkTheme() = runTuIndiceUiTest {
		val scheme = darkColorScheme()
		setMessageInTheme(scheme, agentMessage(id = MESSAGE_ID, useful = false))

		assertGlyphDrawnIn(AssistantUiTags.Message.notUseful(MESSAGE_ID), scheme.inverseSurface)
		assertGlyphNotDrawnIn(AssistantUiTags.Message.notUseful(MESSAGE_ID), scheme.primary)
	}

	@Test
	fun when_reported_then_theFlagIsFilledInTheErrorColor_inLightTheme() = runTuIndiceUiTest {
		val scheme = lightColorScheme()
		setMessageInTheme(scheme, agentMessage(id = MESSAGE_ID, reported = true))

		assertGlyphDrawnIn(AssistantUiTags.Message.report(MESSAGE_ID), scheme.error)
	}

	@Test
	fun when_reported_then_theFlagIsFilledInTheErrorColor_inDarkTheme() = runTuIndiceUiTest {
		val scheme = darkColorScheme()
		setMessageInTheme(scheme, agentMessage(id = MESSAGE_ID, reported = true))

		assertGlyphDrawnIn(AssistantUiTags.Message.report(MESSAGE_ID), scheme.error)
	}

	@Test
	fun when_reported_then_theFlagIsDisabledAndLabelledReportado_andTappingItOpensNoReasons() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantAgentMessageView(
				item = agentMessage(id = MESSAGE_ID, reported = true),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithTag(AssistantUiTags.Message.report(MESSAGE_ID)).assertIsNotEnabled()
		onNodeWithContentDescription(REPORTED_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Message.report(MESSAGE_ID)).performClick()

		assertNodeHidden(AssistantUiTags.Message.reportReason(MESSAGE_ID, AssistantReportReason.HARMFUL))
		assertTrue(actions.isEmpty(), "a reported message sends nothing: $actions")
	}

	// The message in [scheme] on a surface of [scheme], with the theme's colours read from [scheme] itself: the test
	// compares pixels with them. The message sits in a tagged host, which is the node the pixels are captured from.
	private fun ComposeUiTest.setMessageInTheme(scheme: ColorScheme, item: ChatItem.Agent) {
		setTuIndiceTestContent(density = PIXEL_DENSITY) {
			MaterialTheme(colorScheme = scheme) {
				Box(modifier = Modifier.testTag(PIXEL_HOST_TAG).background(scheme.surface)) {
					AssistantAgentMessageView(item = item, onAction = {})
				}
			}
		}
	}

	// The glyph of [tag] is filled in [color]: some pixels inside its touch area are that colour.
	private fun ComposeUiTest.assertGlyphDrawnIn(tag: String, color: Color) {
		assertTrue(countPixelsIn(tag, color) > 0, "no pixel of $color is drawn in $tag")
	}

	private fun ComposeUiTest.assertGlyphNotDrawnIn(tag: String, color: Color) {
		assertEquals(0, countPixelsIn(tag, color), "a pixel of $color is drawn in $tag")
	}

	// The pixels of the host's capture that lie inside the action [tag] and match [color]. The capture and the bounds are
	// both in pixels, measured from the host's top-left corner.
	private fun ComposeUiTest.countPixelsIn(tag: String, color: Color): Int {
		waitForIdle()
		val host = onNodeWithTag(PIXEL_HOST_TAG).fetchSemanticsNode().boundsInRoot
		val action = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
		val pixels = onNodeWithTag(PIXEL_HOST_TAG).captureToImage().toPixelMap()

		val left = (action.left - host.left).toInt().coerceIn(0, pixels.width)
		val right = (action.right - host.left).toInt().coerceIn(0, pixels.width)
		val top = (action.top - host.top).toInt().coerceIn(0, pixels.height)
		val bottom = (action.bottom - host.top).toInt().coerceIn(0, pixels.height)

		var count = 0
		for (x in left until right) {
			for (y in top until bottom) {
				if (pixels[x, y].isCloseTo(color)) count++
			}
		}
		return count
	}

	private fun Color.isCloseTo(other: Color): Boolean =
		abs(red - other.red) <= COLOR_TOLERANCE &&
			abs(green - other.green) <= COLOR_TOLERANCE &&
			abs(blue - other.blue) <= COLOR_TOLERANCE

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val PIXEL_TOLERANCE = 1f

		// Density 1, as the record's pixel tests: a denser content root does not fit the capture surface on the simulator.
		const val PIXEL_DENSITY = 1f
		const val PIXEL_HOST_TAG = "assistant_message_pixel_host"
		const val COLOR_TOLERANCE = 0.05f
		const val REPORTED_DESCRIPTION = "Reportado"
		val ACTION_DESCRIPTIONS = listOf("Copiar", "Útil", "No útil", "Reportar")
	}
}
