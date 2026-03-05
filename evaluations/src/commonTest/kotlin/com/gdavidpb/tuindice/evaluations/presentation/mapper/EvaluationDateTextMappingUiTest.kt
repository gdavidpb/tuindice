package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationDateTextMappingUiTest {
	@Test
	fun when_mappingRemembered_then_providesLabelsForDateGroups() = runTuIndiceUiTest {
		var todayLabel = ""
		var exactDateLabel = ""

		setTuIndiceTestContent {
			val mapping = rememberEvaluationDateTextMapping()

			todayLabel = mapping.todayLabel
			exactDateLabel = EvaluationDateGroup.ExactDate(
				date = LocalDate(2026, 1, 15)
			).getLabel(mapping)

			Text(text = todayLabel)
		}

		onNodeWithText(todayLabel).assertIsDisplayed()
		assertTrue(todayLabel.isNotBlank())
		assertTrue(exactDateLabel.isNotBlank())
	}

	@Test
	fun when_weeksAheadGroupMapped_then_labelIncludesWeeksCount() = runTuIndiceUiTest {
		var weeksAheadLabel = ""
		var noDateLabel = ""

		setTuIndiceTestContent {
			val mapping = rememberEvaluationDateTextMapping()

			weeksAheadLabel = EvaluationDateGroup.WeeksAhead(weeks = 3).getLabel(mapping)
			noDateLabel = EvaluationDateGroup.Continuous.getLabel(mapping)

			Text(text = noDateLabel)
		}

		onNodeWithText(noDateLabel).assertIsDisplayed()
		assertTrue(weeksAheadLabel.contains("3"))
	}
}
