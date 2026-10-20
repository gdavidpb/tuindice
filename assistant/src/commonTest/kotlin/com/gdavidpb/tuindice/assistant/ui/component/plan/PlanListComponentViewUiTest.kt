package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toPlanListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class PlanListComponentViewUiTest {
	@Test
	fun when_plansAreListed_then_eachTermReadsItsSubjectsAndCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanListComponentView(item = plans().toPlanListItem(), rowLimit = null, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Plan.list(COMPONENT_ID))
		onNodeWithText("2026 enero-marzo").assertIsDisplayed()
		onNodeWithText("2 materias · 14 UC").assertIsDisplayed()
		onNodeWithText("2026 abril-junio").assertIsDisplayed()
		onNodeWithText("1 materia · 6 UC").assertIsDisplayed()
		onAllNodesWithText("Ver en Informe").assertCountEquals(2)
	}

	@Test
	fun when_aTermLinkIsTapped_then_itOpensTheRecordOfThatTerm() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			PlanListComponentView(item = plans().toPlanListItem(), rowLimit = null, onAction = { actions.add(it) })
		}

		onAllNodesWithText("Ver en Informe").onFirst().performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.RECORD, open.target)
		assertEquals(listOf(EntityRef(EntityKind.TERM, "2026-JAN_MAR")), open.entityRefs)
	}

	@Test
	fun when_noPlanIsSaved_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PlanListComponentView(
				item = PlanListComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, plans = emptyList()).toPlanListItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("Sin planes guardados").assertIsDisplayed()
	}

	private fun plans(): PlanListComponent =
		PlanListComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			plans = listOf(
				PlanRow("2026-JAN_MAR", "2026 enero-marzo", listOf("IS-101", "IS-102"), credits = 14),
				PlanRow("2026-APR_JUN", "2026 abril-junio", listOf("IS-201"), credits = 6)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Planes de prueba"
	}
}
