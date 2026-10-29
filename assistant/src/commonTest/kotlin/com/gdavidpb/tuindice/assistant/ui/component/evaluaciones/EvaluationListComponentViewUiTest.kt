package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.EvaluationWeeksContext
import com.gdavidpb.tuindice.assistant.presentation.mapper.toEvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.testing.TermCalendarFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.millisOf
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
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
		onNodeWithText("PARCIAL 1").assertIsDisplayed()
		onNodeWithText("14 / 20").assertIsDisplayed()
		onNodeWithText("Completada").assertIsDisplayed()
		onNodeWithText("-- / 20").assertIsDisplayed()
		onNodeWithText("Programada").assertIsDisplayed()
	}

	@Test
	fun when_aRowHasNoDate_then_itsDateLineReadsContinuousAndTheCardHasNoLinkWithoutANamedRecord() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationListComponentView(item = list().toEvaluationListItem(), rowLimit = null, onAction = {})
		}

		onNodeWithText("QUIZ 2").assertIsDisplayed()
		onNodeWithText("Evaluación continua").assertIsDisplayed()
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

	@Test
	fun when_theChatShowsTheFirstRows_then_theyAreTheEvaluationsTabCards() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationListComponentView(item = list().toEvaluationListItem(), rowLimit = 1, onAction = {})
		}

		// The chat draws its first rows with the same card as the sheet and the Evaluations tab: the name in capitals,
		// the subject's chip, the grade over the maximum and the state.
		onNodeWithText("PARCIAL 1").assertIsDisplayed()
		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("14 / 20").assertIsDisplayed()
		onNodeWithText("Completada").assertIsDisplayed()
		onNodeWithText("QUIZ 2").assertDoesNotExist()
	}

	@Test
	fun when_theDeviceHasAnEvaluation_then_theChatAndTheSheetDrawItWithTheEvaluationsTabCard() = runTuIndiceUiTest {
		val item = list().toEvaluationListItem(deviceCards = mapOf("eval-1" to TAB_CARD))
		val rowLimit = mutableStateOf<Int?>(1)

		setTuIndiceTestContent {
			EvaluationListComponentView(item = item, rowLimit = rowLimit.value, onAction = {})
		}

		// The chat shows the tab's card: its name, its state and its place, not what the chat carries for it.
		onNodeWithText("QUIZ 1").assertIsDisplayed()
		onNodeWithText("Sin nota").assertIsDisplayed()
		onNodeWithText("MYS-116").assertIsDisplayed()
		onNodeWithText("PARCIAL 1").assertDoesNotExist()
		onNodeWithText("Completada").assertDoesNotExist()

		rowLimit.value = null
		waitForIdle()

		// The sheet draws the same card, and the evaluation the device lacks from what the chat carries.
		onNodeWithText("QUIZ 1").assertIsDisplayed()
		onNodeWithText("MYS-116").assertIsDisplayed()
		onNodeWithText("QUIZ 2").assertIsDisplayed()
		onNodeWithText("Evaluación continua").assertIsDisplayed()
	}

	@Test
	fun when_theTermIsKnown_then_oneWeekIsShown_andTheStripChangesIt() = runTuIndiceUiTest {
		val item = EvaluationListComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			items = listOf(
				row(evaluationId = "eval-1", title = "Parcial 1", date = LocalDate(2026, 1, 13)),
				row(evaluationId = "eval-2", title = "Parcial 2", date = LocalDate(2026, 1, 21))
			)
		).toEvaluationListItem(
			weeks = EvaluationWeeksContext(
				calendar = TermCalendarFixture(weekOneStart = LocalDate(2026, 1, 12)),
				today = LocalDate(2026, 1, 13)
			)
		)

		setTuIndiceTestContent {
			EvaluationListComponentView(item = item, rowLimit = null, onAction = {})
		}

		// The sheet opens on the week of today, so only its row is on screen.
		onNodeWithText("PARCIAL 1").assertIsDisplayed()
		onNodeWithText("PARCIAL 2").assertDoesNotExist()

		onNodeWithTag(AssistantUiTags.Evaluations.weekChip("week_2")).performClick()
		waitForIdle()

		onNodeWithText("PARCIAL 2").assertIsDisplayed()
		onNodeWithText("PARCIAL 1").assertDoesNotExist()
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

	private fun row(evaluationId: String, title: String, date: LocalDate): EvaluationRow =
		EvaluationRow(
			evaluationId = evaluationId,
			subjectCode = "IS-101",
			title = title,
			dateMillis = millisOf(date),
			scheduleMode = "dated",
			grade = null,
			maxGrade = 20.0,
			state = "pending"
		)

	private companion object {
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Evaluaciones de prueba"
		const val DATE_MILLIS = 1_760_000_000_000L
		val EVALUATION_REF = EntityRef(EntityKind.EVALUATION, "eval-1")

		// The card the Evaluations tab shows for eval-1: a quiz in its class's room whose date passed with no grade.
		val TAB_CARD = EvaluationCardItem(
			name = "QUIZ 1",
			subjectCode = "IS-101",
			dateText = UiText.Raw("Miércoles 21/01/26"),
			highlightTone = EvaluationTone.Error,
			typeIcon = Icons.Outlined.Quiz,
			place = ClassPlaceItem(room = "MYS-116", scheduleText = UiText.Raw("1-2")),
			gradeText = "-- / 20",
			statusText = "Sin nota",
			statusTone = EvaluationTone.Error
		)
	}
}
