package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class CooldownComponentViewUiTest {
	@Test
	fun when_appealable_then_theAppealSendsTheWrittenText() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			CooldownComponentView(
				component = component(appealable = true),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.AppealAction)
		onNodeWithTag(AssistantUiTags.AppealAction).performClick()
		assertNodeVisible(AssistantUiTags.AppealText)
		onNodeWithTag(AssistantUiTags.AppealText).performTextInput(APPEAL)
		onNodeWithTag(AssistantUiTags.AppealSend).performClick()

		assertEquals(APPEAL, assertIs<AssistantChat.Action.Appeal>(actions.single()).text)
	}

	@Test
	fun when_notAppealable_then_offersNoAppeal() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CooldownComponentView(component = component(appealable = false), onAction = {})
		}

		onNodeWithText(TITLE).assertIsDisplayed()
		assertNodeHidden(AssistantUiTags.AppealAction)
	}

	private fun component(appealable: Boolean): CooldownComponent =
		CooldownComponent(
			id = "component-1",
			fallbackText = TITLE,
			reason = AbuseReasonCode.SPAM,
			untilMillis = UNTIL_MILLIS,
			appealable = appealable
		)

	private companion object {
		const val TITLE = "El asistente está en pausa"
		const val APPEAL = "Solo hice una pregunta"
		const val UNTIL_MILLIS = 1_800_000_000_000L
	}
}
