package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AssistantAppealDialogUiTest {
	@Test
	fun when_textIsEmpty_then_sendIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAppealDialog(onAppeal = {}, onDismissRequest = {})
		}

		assertNodeVisible(AssistantUiTags.AppealSend)
		assertNodeDisabled(AssistantUiTags.AppealSend)
	}

	@Test
	fun when_textIsWritten_then_sendDispatchesTheAppealAndCountsTheCharacters() = runTuIndiceUiTest {
		val appeals = mutableListOf<String>()
		setTuIndiceTestContent {
			AssistantAppealDialog(onAppeal = { appeals.add(it) }, onDismissRequest = {})
		}

		assertNodeVisible(AssistantUiTags.AppealText)
		onNodeWithTag(AssistantUiTags.AppealText).performTextInput(APPEAL)
		onNodeWithTag(AssistantUiTags.AppealCounter).assertTextEquals("$APPEAL_LENGTH\u00A0/\u00A0500")
		assertNodeEnabled(AssistantUiTags.AppealSend)
		onNodeWithTag(AssistantUiTags.AppealSend).performClick()

		assertEquals(listOf(APPEAL), appeals)
	}

	private companion object {
		const val APPEAL = "Creo que es un error"
		const val APPEAL_LENGTH = 20
	}
}
