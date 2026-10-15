package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluationTitleLineUiTest {
	@Test
	fun when_theEvaluationHasNoState_then_theLineHoldsOnlyItsName() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem(titleText = "PARCIAL 1")

		setTuIndiceTestContent {
			SummaryEvaluationTitleLine(item = item)
		}

		onNodeWithText("PARCIAL 1").assertIsDisplayed()
		onAllNodesWithTag(SummaryEvaluationRowUiTags.statusChip(item.evaluationId)).assertCountEquals(0)
	}

	@Test
	fun when_theEvaluationHasAState_then_itsChipEndsTheLine() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem(titleText = "PARCIAL 1").copy(statusText = UiText.Raw("Vence hoy"))

		setTuIndiceTestContent {
			SummaryEvaluationTitleLine(item = item)
		}

		onNodeWithText("Vence hoy").assertIsDisplayed()
	}
}
