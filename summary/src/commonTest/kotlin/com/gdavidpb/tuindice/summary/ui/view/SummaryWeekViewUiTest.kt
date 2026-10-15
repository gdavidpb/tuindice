package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryWeekItem
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryWeekViewUiTest {
	private val week = summaryHubItem().week!!

	@Test
	fun when_weekHasAClassAndEvaluations_then_theClassIsFirstAndEveryRowIsThere() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week, actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.WeekSection)
		onNodeWithText("Esta semana").assertIsDisplayed()
		onNodeWithText("Horario").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.WeekClassRow)
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-2"))
	}

	@Test
	fun when_weekHasAClassAndScheduledEvaluations_then_theClassIsTitledClase_andNoRowHasAChip() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SummaryWeekView(week = week, actions = SummaryHubActions())
			}

			onNodeWithText("CLASE", useUnmergedTree = true).assertIsDisplayed()
			onNodeWithTag(SummaryEvaluationRowUiTags.statusChip("evaluation-1"), useUnmergedTree = true)
				.assertDoesNotExist()
			onNodeWithTag(SummaryEvaluationRowUiTags.statusChip("evaluation-2"), useUnmergedTree = true)
				.assertDoesNotExist()
		}

	@Test
	fun when_theTermHasNoEvaluationYet_then_theCardUnderTheClassInvitesToAddOne_andItsButtonAsksForTheForm() =
		runTuIndiceUiTest {
			val asked = mutableListOf<SummaryHubTarget>()

			setTuIndiceTestContent {
				SummaryWeekView(
					week = week.copy(
						evaluations = emptyList(),
						card = SummaryCardItem(
							icon = SummaryCardIcon.Evaluations,
							titleText = UiText.Raw("Agrega tus evaluaciones"),
							messageText = UiText.Raw("Parciales, quices y proyectos: los verás aquí y llevarás tus puntos."),
							action = SummaryCardItem.Action(
								text = UiText.Raw("Agregar evaluación"),
								target = SummaryHubTarget.AddEvaluation
							)
						)
					),
					actions = SummaryHubActions(onTargetClick = { target -> asked += target })
				)
			}

			assertNodeVisible(SummaryUiTags.WeekClassRow)
			assertNodeVisible(SummaryUiTags.WeekCard)
			onNodeWithText("Agrega tus evaluaciones").assertIsDisplayed()
			onNodeWithTag(SummaryUiTags.CardAction).performClick()
			assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.AddEvaluation), asked)
		}

	@Test
	fun when_theWeekHasNoRows_then_itsCardSaysWhatItLacks_andNoRowIsThere() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(
				week = SummaryWeekItem(
					classRow = null,
					evaluations = emptyList(),
					hasSchedule = false,
					card = SummaryCardItem(
						icon = SummaryCardIcon.Calendar,
						titleText = UiText.Raw("Sin clases esta semana"),
						messageText = UiText.Raw("Cuando inscribas, aquí verás tus clases y evaluaciones.")
					)
				),
				actions = SummaryHubActions()
			)
		}

		onNodeWithTag(SummaryUiTags.WeekClassRow).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.WeekCard).assertHasNoClickAction()
		onNodeWithText("Sin clases esta semana").assertIsDisplayed()
	}

	@Test
	fun when_weekHasNoClass_then_onlyTheEvaluationsAreListed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week.copy(classRow = null), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.WeekClassRow).assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
	}

	@Test
	fun when_theTermHasNoSchedule_then_theWeekOffersNoLinkToIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryWeekView(week = week.copy(classRow = null, hasSchedule = false), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).assertDoesNotExist()
		onNodeWithText("Horario").assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.weekEvaluationRow("evaluation-1"))
	}

	@Test
	fun when_rowsAndTheLinkAreTapped_then_eachAsksForItsScreen() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryWeekView(
				week = week,
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).performClick()
		onNodeWithTag(SummaryUiTags.WeekClassRow).performClick()
		onNodeWithTag(SummaryUiTags.weekEvaluationRow("evaluation-1")).performClick()

		assertEquals(
			listOf(
				SummaryHubTarget.Schedule,
				SummaryHubTarget.Schedule,
				SummaryHubTarget.EvaluationsWeek(
					weekStart = LocalDate(2026, 10, 12),
					evaluationId = "evaluation-1"
				)
			),
			targets
		)
	}

	@Test
	fun when_theClassIsInProgressOnANarrowScreen_then_theLeadsAreOneWidth_andTheContentStartsAfterIt() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryWeekView(week = week, actions = SummaryHubActions())
				}
			}

			// The pill of the class and the day that leads the evaluation are one width, and the content of each row
			// starts after its lead: the subject of the class, the type icon of the evaluation. The subject of the
			// evaluation sits under its name with no indent, and its place beside the chip.
			val pill = onNodeWithTag(SummaryUiTags.WeekClassPill, useUnmergedTree = true).fetchSemanticsNode()
			val day = onNodeWithTag(SummaryEvaluationRowUiTags.lead("evaluation-1"), useUnmergedTree = true)
				.fetchSemanticsNode()
			val subject = onAllNodesWithText("CI5437", useUnmergedTree = true).fetchSemanticsNodes().first()
			val icon = onNodeWithTag(SummaryEvaluationRowUiTags.typeIcon("evaluation-1"), useUnmergedTree = true)
				.fetchSemanticsNode()

			assertTrue(
				kotlin.math.abs(pill.boundsInRoot.width - day.boundsInRoot.width) < 0.5f,
				"the pill is ${pill.boundsInRoot.width} px wide and the day of the evaluation ${day.boundsInRoot.width} px"
			)
			assertTrue(
				subject.boundsInRoot.left >= pill.boundsInRoot.right,
				"the class starts its content at ${subject.boundsInRoot.left} px, after its pill"
			)
			assertTrue(
				icon.boundsInRoot.left >= day.boundsInRoot.right,
				"the evaluation starts its content at ${icon.boundsInRoot.left} px, after its day"
			)
		}
}
