package com.gdavidpb.tuindice.assistant.ui.component.resumen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toAttentionCardItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.hasLinkLabel
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
	fun when_anItemNamesAnEvaluation_then_itsRowOpensTheEvaluation() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AttentionComponentView(
				item = attention(item(EVALUATION_REF, title = "Parcial 2 sin nota")).toAttentionCardItem(),
				rowLimit = null,
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.Summary.attention(COMPONENT_ID))
		onNodeWithText("Parcial 2 sin nota").assertIsDisplayed()
		onNode(hasLinkLabel("Ver evaluación Parcial 2 sin nota")).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.EVALUATIONS_DETAIL, open.target)
		assertEquals(listOf(EVALUATION_REF), open.entityRefs)
	}

	@Test
	fun when_anItemNamesATerm_then_itsRowOpensTheRecord() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AttentionComponentView(
				item = attention(item(TERM_REF, title = "Trimestre sin cerrar")).toAttentionCardItem(),
				rowLimit = null,
				onAction = { actions.add(it) }
			)
		}

		onNode(hasLinkLabel("Ver en Informe")).performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.RECORD, open.target)
		assertEquals(listOf(TERM_REF), open.entityRefs)
	}

	@Test
	fun when_anItemNamesAPlan_then_itsRowIsNotATap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttentionComponentView(
				item = attention(item(PLAN_REF, title = "Plan por revisar")).toAttentionCardItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("Plan por revisar").assertIsDisplayed()
		onAllNodes(hasClickAction()).assertCountEquals(0)
	}

	@Test
	fun when_nothingNeedsAttention_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttentionComponentView(
				item = attention().toAttentionCardItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("Nada que atender por ahora").assertIsDisplayed()
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
