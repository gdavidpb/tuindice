package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluationStatusChipUiTest {
	@Test
	fun when_theEvaluationIsNotOverdue_then_theChipShowsItsText() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem()

		setTuIndiceTestContent {
			SummaryEvaluationStatusChip(item = item, text = "Programada")
		}

		onNodeWithTag(SummaryEvaluationRowUiTags.statusChip(item.evaluationId)).assertExists()
		onNodeWithText("Programada").assertIsDisplayed()
	}

	@Test
	fun when_theEvaluationIsOverdue_then_theChipShowsItsTextInTheErrorTone() = runTuIndiceUiTest {
		val item = summaryEvaluationRowItem().copy(statusText = UiText.Raw("Vencida"), isOverdue = true)

		setTuIndiceTestContent {
			SummaryEvaluationStatusChip(item = item, text = "Vencida")
		}

		onNodeWithText("Vencida").assertIsDisplayed()
	}
}
