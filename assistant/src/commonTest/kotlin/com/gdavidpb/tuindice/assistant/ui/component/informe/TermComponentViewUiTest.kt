package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.TermSubjectRow
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class TermComponentViewUiTest {
	@Test
	fun when_theTermHasIndexes_then_theTermIndexIsTheFigure_andTheRowsReadTheirGradesAndResults() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermComponentView(component = term(), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Report.term(COMPONENT_ID))
		onNodeWithText("2026 ENERO-MARZO").assertIsDisplayed()
		onNodeWithContentDescription("Índice del trimestre: 16,2000").assertIsDisplayed()
		onNodeWithContentDescription("Acumulado: 15,8000").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Programación I").assertIsDisplayed()
		onNodeWithText("18").assertIsDisplayed()
		onNodeWithText("IS-102").assertIsDisplayed()
		onNodeWithText("En curso").assertIsDisplayed()
		onNodeWithText("Sin efecto").assertIsDisplayed()
	}

	@Test
	fun when_theCardNamesItsTerm_then_theLinkOpensTheRecordWithItsRefs() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			TermComponentView(component = term(), onAction = { actions.add(it) })
		}

		onNodeWithText("Ver en Informe").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.RECORD, open.target)
		assertEquals(listOf(TERM_REF), open.entityRefs)
	}

	@Test
	fun when_theCardNamesNoTerm_then_thereIsNoLink() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermComponentView(component = term(entityRefs = emptyList()), onAction = {})
		}

		onNodeWithText("Ver en Informe").assertDoesNotExist()
	}

	private fun term(entityRefs: List<EntityRef> = listOf(TERM_REF)): TermComponent =
		TermComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			termId = "2026-JAN_MAR",
			termLabel = "2026 enero-marzo",
			termIndex = 16.2,
			cumulativeIndex = 15.8,
			credits = 14,
			rows = listOf(
				TermSubjectRow(
					attemptId = "attempt-1",
					subjectCode = "IS-101",
					subjectName = "Programación I",
					credits = 6,
					grade = 18,
					outcome = "approved",
					badge = "first_try"
				),
				TermSubjectRow(
					attemptId = "attempt-2",
					subjectCode = "IS-102",
					subjectName = "Matemática I",
					credits = 8,
					grade = null,
					outcome = "pending",
					badge = "without_effect"
				)
			),
			entityRefs = entityRefs
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Trimestre de prueba"
		val TERM_REF = EntityRef(EntityKind.TERM, "2026-JAN_MAR")
	}
}
