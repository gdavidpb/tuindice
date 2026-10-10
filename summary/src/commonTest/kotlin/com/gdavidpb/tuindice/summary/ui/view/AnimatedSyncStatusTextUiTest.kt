package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

/**
 * Only what the text settles on is asserted. The transition itself (fade plus a vertical
 * slide of a third of the height, and its duration) is drawing and timing, not behaviour.
 */
@OptIn(ExperimentalTestApi::class)
class AnimatedSyncStatusTextUiTest {
	@Test
	fun when_textIsProvided_then_showsItUnderTheStatusTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AnimatedSyncStatusText(text = "Sincronizando…")
		}

		assertNodeVisible(SummaryUiTags.StatusText)
		onNodeWithTag(SummaryUiTags.StatusText).assertTextEquals("Sincronizando…")
	}

	@Test
	fun when_textChanges_then_settlesOnTheNewTextAndDropsTheOldOne() = runTuIndiceUiTest {
		var text by mutableStateOf("Sincronizando…")

		setTuIndiceTestContent {
			AnimatedSyncStatusText(text = text)
		}

		assertNodeVisible(SummaryUiTags.StatusText)

		runOnIdle { text = "Todo al día" }
		waitForIdle()

		onAllNodesWithTag(SummaryUiTags.StatusText).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.StatusText).assertTextEquals("Todo al día")
		onNodeWithText("Sincronizando…").assertDoesNotExist()
	}

	@Test
	fun when_modifierIsProvided_then_itWrapsTheStatusText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AnimatedSyncStatusText(
				text = "Sin conexión",
				modifier = Modifier.testTag(HOST_TAG)
			)
		}

		assertNodeVisible(HOST_TAG)
		onNode(
			hasTestTag(SummaryUiTags.StatusText) and hasAnyAncestor(hasTestTag(HOST_TAG))
		).assertTextEquals("Sin conexión")
	}

	private companion object {
		const val HOST_TAG = "animated_sync_status_text_host"
	}
}
