package com.gdavidpb.tuindice.assistant.ui.component.resumen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.AttentionItem
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
class AttentionComponentViewUiTest {
	@Test
	fun when_anItemNamesAnEvaluation_then_itsLinkOpensTheEvaluation() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AttentionComponentView(
				component = attention(item(EVALUATION_REF, title = "Parcial 2 sin nota")),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.Summary.attention(COMPONENT_ID))
		onNodeWithContentDescription("Avisos: 1").assertIsDisplayed()
		onNodeWithText("Parcial 2 sin nota").assertIsDisplayed()
		onNodeWithText("Ver evaluación").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.EVALUATIONS_DETAIL, open.target)
		assertEquals(listOf(EVALUATION_REF), open.entityRefs)
	}

	@Test
	fun when_anItemNamesATerm_then_itsLinkOpensTheRecord() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AttentionComponentView(
				component = attention(item(TERM_REF, title = "Trimestre sin cerrar")),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithText("Ver en Informe").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.RECORD, open.target)
		assertEquals(listOf(TERM_REF), open.entityRefs)
	}

	@Test
	fun when_anItemNamesAPlan_then_itHasNoLink() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttentionComponentView(
				component = attention(item(PLAN_REF, title = "Plan por revisar")),
				onAction = {}
			)
		}

		onNodeWithText("Plan por revisar").assertIsDisplayed()
		onNodeWithText("Ver evaluación").assertDoesNotExist()
		onNodeWithText("Ver en Informe").assertDoesNotExist()
	}

	private fun attention(vararg items: AttentionItem): AttentionComponent =
		AttentionComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			items = items.toList()
		)

	private fun item(ref: EntityRef, title: String): AttentionItem =
		AttentionItem(
			kind = "attention_${ref.kind.name.lowercase()}",
			title = title,
			detail = DETAIL,
			entityRef = ref
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Atención de prueba"
		const val DETAIL = "CI5437 · venció el lunes"
		val EVALUATION_REF = EntityRef(EntityKind.EVALUATION, "eval-1")
		val TERM_REF = EntityRef(EntityKind.TERM, "2026-JAN_MAR")
		val PLAN_REF = EntityRef(EntityKind.PLAN, "plan-1")
	}
}
