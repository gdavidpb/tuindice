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
	fun when_evaluationHasAPlace_then_rowShowsDayIconNameSubjectAndBlocksButNoHour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(item = summaryEvaluationRowItem(), onClick = {})
		}

		onNodeWithText("Hoy", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithTag(SummaryEvaluationRowUiTags.typeIcon("evaluation-1"), useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("PARCIAL 1", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("CI5437", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertIsDisplayed()
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
	fun when_theRowIsDrawn_then_theSubjectStartsWhereTheNameStarts_andThePlaceSharesItsLine() = runTuIndiceUiTest {
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
		val place = onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		assertTrue(name.left > icon.left, "the name is written after the icon")
		assertTrue(subject.top >= name.bottom, "the subject is on the line under the name")
		// The chip's box starts where the icon does, and its code sits inside the padding, less than the icon wide.
		assertTrue(
			subject.left >= icon.left - 1f && subject.left - icon.left < icon.width,
			"the subject starts at the edge of the column, its code inside the chip's padding"
		)
		assertTrue(place.left >= subject.right, "the place is written after the subject")
		assertTrue(abs(place.center.y - subject.center.y) <= 1f, "the place shares the line of the subject")
	}

	@Test
	fun when_theLeadIsDrawn_then_itsBaselineIsTheBaselineOfTheName_aboveTheSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryEvaluationRowView(
				item = summaryEvaluationRowItem().copy(statusText = UiText.Raw("Completada")),
				onClick = {}
			)
		}

		val day = onNodeWithTag(SummaryEvaluationRowUiTags.lead("evaluation-1"), useUnmergedTree = true)
			.fetchSemanticsNode()
		val name = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode()
		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		val dayBaseline = day.firstBaselineInRoot()
		val nameBaseline = name.firstBaselineInRoot()

		assertTrue(
			abs(dayBaseline - nameBaseline) <= 1f,
			"the day's baseline is $dayBaseline and the name's is $nameBaseline"
		)
		assertTrue(dayBaseline < subject.top, "the day's baseline is on the first line, above the subject")
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
		onNodeWithText("3-4 · MYS-210", useUnmergedTree = true).assertDoesNotExist()
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
		assertTextIsWhole("3-4 · MYS-210")
	}
}
