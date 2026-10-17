package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class HelpComponentViewUiTest {
	@Test
	fun when_helpHasSteps_then_theyAreNumbered_andTheButtonOpensTheScreenWithItsRefs() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			HelpComponentView(component = help(), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Knowledge.help(COMPONENT_ID))
		onNodeWithText("AYUDA").assertIsDisplayed()
		onNodeWithText("Cómo ver tu índice").assertIsDisplayed()
		onNodeWithText("1.").assertIsDisplayed()
		onNodeWithText("Abre el informe.").assertIsDisplayed()
		onNodeWithText("2.").assertIsDisplayed()
		onNodeWithText("Elige el trimestre.").assertIsDisplayed()

		onNodeWithTag(AssistantUiTags.Knowledge.helpOpen(COMPONENT_ID)).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.RECORD, open.target)
		assertEquals(listOf(TERM_REF), open.entityRefs)
	}

	@Test
	fun when_helpNamesNoScreen_then_thereIsNoButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			HelpComponentView(component = help(target = null), onAction = {})
		}

		onNodeWithText("Ir a la pantalla").assertDoesNotExist()
	}

	private fun help(target: NavTarget? = NavTarget.RECORD): HelpComponent =
		HelpComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = listOf(TERM_REF),
			title = "Cómo ver tu índice",
			steps = listOf("Abre el informe.", "Elige el trimestre."),
			target = target
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Ayuda de prueba"
		val TERM_REF = EntityRef(EntityKind.TERM, "2026-JAN_MAR")
	}
}
