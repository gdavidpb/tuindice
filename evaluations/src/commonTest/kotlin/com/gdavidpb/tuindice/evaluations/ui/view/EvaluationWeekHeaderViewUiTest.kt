package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationWeekHeaderViewUiTest {
	@Test
	fun when_weekIsAcademic_then_headerIsTaggedByItsWeekNumber_andReadsLabelAndCount() = runTuIndiceUiTest {
		val weekHeaderTag = EvaluationsUiTags.evaluationsWeekHeader(8)

		setTuIndiceTestContent {
			EvaluationWeekHeaderView(
				weekKey = EvaluationsWeekKey.Academic(8),
				label = "Semana 8",
				countText = "2 evaluaciones"
			)
		}

		assertNodeVisible(weekHeaderTag)
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(7))
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(EvaluationsWeekKey.Continuous))
		onNodeWithTag(EvaluationsUiTags.evaluationHeader("Semana 8")).assertIsDisplayed()
		onNode(hasText("Semana 8") and hasAnyAncestor(hasTestTag(weekHeaderTag))).assertIsDisplayed()
		onNode(hasText("2 evaluaciones") and hasAnyAncestor(hasTestTag(weekHeaderTag))).assertIsDisplayed()
	}

	@Test
	fun when_weekIsContinuous_then_headerIsTaggedAsContinuous_andReadsLabelAndCount() = runTuIndiceUiTest {
		val weekHeaderTag = EvaluationsUiTags.evaluationsWeekHeader(EvaluationsWeekKey.Continuous)

		setTuIndiceTestContent {
			EvaluationWeekHeaderView(
				weekKey = EvaluationsWeekKey.Continuous,
				label = "Continuas",
				countText = "1 evaluación"
			)
		}

		assertNodeVisible(weekHeaderTag)
		assertNodeHidden(EvaluationsUiTags.evaluationsWeekHeader(1))
		onNodeWithTag(EvaluationsUiTags.evaluationHeader("Continuas")).assertIsDisplayed()
		onNode(hasText("Continuas") and hasAnyAncestor(hasTestTag(weekHeaderTag))).assertIsDisplayed()
		onNode(hasText("1 evaluación") and hasAnyAncestor(hasTestTag(weekHeaderTag))).assertIsDisplayed()
	}
}
