package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.firstBaselineInRoot
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluationRowViewUiTest {
	@Test
	fun when_evaluationHasAPlace_then_rowShowsDayIconNameSubjectRoomAndBlocksButNoHour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(item = summaryEvaluationRowItem(), onClick = {})
		}

		onNodeWithText("Hoy", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithTag(SummaryEvaluationRowUiTags.typeIcon("evaluation-1"), useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("PARCIAL 1", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("CI5437", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("MYS-210", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("3-4", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("9:30–11:30 a. m.", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_evaluationIsScheduled_then_itHasNoStatusChip_andNoProgramadaWord() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(item = summaryEvaluationRowItem(), onClick = {})
		}

		onNodeWithTag(SummaryEvaluationRowUiTags.statusChip("evaluation-1"), useUnmergedTree = true)
			.assertDoesNotExist()
		onNodeWithText("Programada", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_evaluationIsCompleted_then_itsStatusChipSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				item = summaryEvaluationRowItem().copy(statusText = UiText.Raw("Completada")),
				onClick = {}
			)
		}

		onNodeWithTag(SummaryEvaluationRowUiTags.statusChip("evaluation-1"), useUnmergedTree = true)
			.assertIsDisplayed()
		onNodeWithText("Completada", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("Programada", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_theRowIsDrawn_then_theSubjectIsBesideTheName_andThePlaceStartsUnderIt() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Expanded) {
			Box(modifier = Modifier.width(600.dp)) {
				SummaryEvaluationRowView(
					item = summaryEvaluationRowItem().copy(statusText = UiText.Raw("Completada")),
					onClick = {}
				)
			}
		}

		val icon = onNodeWithTag(SummaryEvaluationRowUiTags.typeIcon("evaluation-1"), useUnmergedTree = true)
			.fetchSemanticsNode().boundsInRoot
		val name = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val schedule = onNodeWithText("3-4", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(name.left > icon.left, "the name is written after the icon")
		assertTrue(subject.left >= name.right, "the subject is written after the name")
		assertTrue(abs(subject.center.y - name.center.y) <= 1f, "the subject shares the line of the name")
		assertTrue(room.top >= name.bottom, "the room is on the line under the name")
		assertTrue(abs(room.left - name.left) <= 1f, "the room starts where the name starts")
		assertTrue(schedule.top >= room.bottom, "the schedule is under the room")
	}

	@Test
	fun when_theLeadIsDrawn_then_itsBaselineIsTheBaselineOfTheName_aboveThePlace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				item = summaryEvaluationRowItem().copy(statusText = UiText.Raw("Completada")),
				onClick = {}
			)
		}

		val day = onNodeWithTag(SummaryEvaluationRowUiTags.lead("evaluation-1"), useUnmergedTree = true)
			.fetchSemanticsNode()
		val name = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode()
		val room = onNodeWithText("MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		val dayBaseline = day.firstBaselineInRoot()
		val nameBaseline = name.firstBaselineInRoot()

		assertTrue(
			abs(dayBaseline - nameBaseline) <= 1f,
			"the day's baseline is $dayBaseline and the name's is $nameBaseline"
		)
		assertTrue(dayBaseline < room.top, "the day's baseline is on the first line, above the place")
	}

	@Test
	fun when_subjectHasNoClassThatDay_then_rowHasNoPlaceLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				item = summaryEvaluationRowItem(
					leadText = "Vie 16",
					titleText = "ENSAYO 1",
					subjectCode = "EG1114",
					hasPlace = false
				),
				onClick = {}
			)
		}

		onNodeWithText("Vie 16", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("ENSAYO 1", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("MYS-210", useUnmergedTree = true).assertDoesNotExist()
		onNodeWithText("3-4", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_rowIsTapped_then_itAsksForItsWeek_andSaysSoToAScreenReader() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				modifier = Modifier.testTag("row"),
				item = summaryEvaluationRowItem(),
				onClick = { taps++ }
			)
		}

		onNodeWithTag("row").assertHasClickAction()
		assertEquals(
			"Ver las evaluaciones de esa semana",
			onNodeWithTag("row").fetchSemanticsNode().config[SemanticsActions.OnClick].label
		)

		onNodeWithTag("row").performClick()

		assertEquals(1, taps)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_noTextIsCutAndTheChipIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryEvaluationRowView(
						item = summaryEvaluationRowItem(titleText = "TRABAJO ESCRITO 12", leadText = "Mié 28")
							.copy(statusText = UiText.Raw("Sin nota")),
						onClick = {}
					)
				}
			}
		}

		assertTextIsWhole("Mié 28", singleLine = true)
		assertTextIsWhole("TRABAJO ESCRITO 12")
		assertTextIsWhole("Sin nota", singleLine = true)
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("MYS-210", singleLine = true)
		assertTextIsWhole("3-4", singleLine = true)
	}
}
