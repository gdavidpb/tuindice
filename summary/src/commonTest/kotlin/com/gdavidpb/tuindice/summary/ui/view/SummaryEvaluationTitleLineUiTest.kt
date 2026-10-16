package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluationTitleLineUiTest {
	@Test
	fun when_theEvaluationHasNoState_then_theLineHoldsOnlyItsNameAndItsSubject() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem(titleText = "PARCIAL 1")

		setTuIndiceTestContent {
			SummaryEvaluationTitleLine(item = item)
		}

		onNodeWithText("PARCIAL 1").assertIsDisplayed()
		onNodeWithText("CI5437").assertIsDisplayed()
		onAllNodesWithTag(SummaryEvaluationRowUiTags.statusChip(item.evaluationId)).assertCountEquals(0)
	}

	@Test
	fun when_theEvaluationHasAState_then_itsChipEndsTheLine_andTheNameAndSubjectStayBeforeIt() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem(titleText = "PARCIAL 1").copy(statusText = UiText.Raw("Vence hoy"))

		setTuIndiceTestContent {
			Box(modifier = Modifier.width(360.dp).testTag(LINE_TAG)) {
				SummaryEvaluationTitleLine(item = item)
			}
		}

		val line = onNodeWithTag(LINE_TAG, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val chip = onNodeWithTag(
			testTag = SummaryEvaluationRowUiTags.statusChip(item.evaluationId),
			useUnmergedTree = true
		).fetchSemanticsNode().boundsInRoot
		val subject = onNodeWithText("CI5437", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		onNodeWithText("Vence hoy").assertIsDisplayed()
		assertTrue(abs(chip.right - line.right) <= 1f, "the state ends the line: chip ${chip.right}, line ${line.right}")
		assertTrue(subject.right <= chip.left, "the subject is written before the state")
	}

	@Test
	fun when_theLetterIsLargeOnA320dpScreen_then_theNameKeepsItsWidth_andTheStateStaysOnItsFirstLine() =
		runTuIndiceUiTest {
			val item = summaryEvaluationRowItem(titleText = "TRABAJO ESCRITO 12")
				.copy(statusText = UiText.Raw("Sin nota"))

			setTuIndiceTestContent {
				WithLargeLetter {
					Box(modifier = Modifier.width(200.dp)) {
						SummaryEvaluationTitleLine(item = item)
					}
				}
			}

			val name = onNodeWithText("TRABAJO ESCRITO 12", useUnmergedTree = true)
			val layouts = mutableListOf<TextLayoutResult>()

			name.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)

			val nameBounds = name.fetchSemanticsNode().boundsInRoot
			val firstLineCenter = nameBounds.top + (layouts.single().getLineTop(0) + layouts.single().getLineBottom(0)) / 2f
			val state = onNodeWithTag(
				testTag = SummaryEvaluationRowUiTags.statusChip(item.evaluationId),
				useUnmergedTree = true
			).fetchSemanticsNode().boundsInRoot

			assertTextIsWhole("TRABAJO ESCRITO 12")
			assertTextIsWhole("CI5437", singleLine = true)
			assertTrue(nameBounds.width >= NAME_MIN_WIDTH, "the name keeps its width: ${nameBounds.width}")
			assertTrue(
				abs(state.center.y - firstLineCenter) <= 6f,
				"the state is centered at ${state.center.y}, not on the first line of the name at $firstLineCenter"
			)
		}

	@Test
	fun when_theStateIsGiven_then_itIsLevelWithTheNameAtNormalWidth() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem(titleText = "PARCIAL 1").copy(statusText = UiText.Raw("Completada"))

		setTuIndiceTestContent {
			Box(modifier = Modifier.width(360.dp)) {
				SummaryEvaluationTitleLine(item = item)
			}
		}

		val name = onNodeWithText("PARCIAL 1", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
		val state = onNodeWithTag(
			testTag = SummaryEvaluationRowUiTags.statusChip(item.evaluationId),
			useUnmergedTree = true
		).fetchSemanticsNode().boundsInRoot

		assertTrue(abs(state.center.y - name.center.y) <= 1.5f, "the state and the name share the line")
	}

	private companion object {
		const val LINE_TAG = "evaluation_title_line"
		const val NAME_MIN_WIDTH = 40f
	}
}
