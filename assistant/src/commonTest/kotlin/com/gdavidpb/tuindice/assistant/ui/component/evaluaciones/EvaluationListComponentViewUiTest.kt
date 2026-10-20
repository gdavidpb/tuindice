package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toEvaluationListItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
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
class EvaluationListComponentViewUiTest {
	@Test
	fun when_evaluationsAreListed_then_eachRowShowsItsGradeAndState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationListComponentView(item = list().toEvaluationListItem(), rowLimit = null, onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Evaluations.list(COMPONENT_ID))
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("Parcial 1", substring = true).assertIsDisplayed()
		onNodeWithText("14\u00A0/\u00A020").assertIsDisplayed()
		onNodeWithText("Calificada").assertIsDisplayed()
		onNodeWithText("—\u00A0/\u00A020").assertIsDisplayed()
		onNodeWithText("Pendiente").assertIsDisplayed()
	}

	@Test
	fun when_aRowHasNoDate_then_itsTitleReadsAloneAndTheCardHasNoLinkWithoutANamedRecord() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationListComponentView(item = list().toEvaluationListItem(), rowLimit = null, onAction = {})
		}

		onNodeWithText("Quiz 2").assertIsDisplayed()
		onNodeWithText("Ver evaluación").assertDoesNotExist()
	}

	@Test
	fun when_theCardNamesOneEvaluation_then_theLinkOpensItsDetailWithItsRef() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			EvaluationListComponentView(
				item = list(refs = listOf(EVALUATION_REF)).toEvaluationListItem(),
				rowLimit = null,
				onAction = { actions.add(it) }
			)
		}

		onNodeWithText("Ver evaluación").performClick()

		val open = assertIs<AssistantChat.Action.OpenNavTarget>(actions.single())
		assertEquals(NavTarget.EVALUATIONS_DETAIL, open.target)
		assertEquals(listOf(EVALUATION_REF), open.entityRefs)
	}

	@Test
	fun when_noEvaluationIsListed_then_aLineSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationListComponentView(
				item = EvaluationListComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT, items = emptyList())
					.toEvaluationListItem(),
				rowLimit = null,
				onAction = {}
			)
		}

		onNodeWithText("No hay evaluaciones para mostrar").assertIsDisplayed()
	}

	private fun list(refs: List<EntityRef> = emptyList()): EvaluationListComponent =
		EvaluationListComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			entityRefs = refs,
			items = listOf(
				EvaluationRow(
					evaluationId = "eval-1",
					subjectCode = "IS-101",
					title = "Parcial 1",
					dateMillis = DATE_MILLIS,
					scheduleMode = "dated",
					grade = 14.0,
					maxGrade = 20.0,
					state = "graded"
				),
				EvaluationRow(
					evaluationId = "eval-2",
					subjectCode = "IS-102",
					title = "Quiz 2",
					dateMillis = null,
					scheduleMode = "undated",
					grade = null,
					maxGrade = 20.0,
					state = "pending"
				)
			)
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Evaluaciones de prueba"
		const val DATE_MILLIS = 1_760_000_000_000L
		val EVALUATION_REF = EntityRef(EntityKind.EVALUATION, "eval-1")
	}
}
