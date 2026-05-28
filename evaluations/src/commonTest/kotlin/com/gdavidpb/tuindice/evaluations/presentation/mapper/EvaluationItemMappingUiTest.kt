package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationItemMappingUiTest {
	@Test
	fun when_mappingRemembered_then_exposesTypeLabelsAndPatterns() = runTuIndiceUiTest {
		var quizLabel = ""
		var mappedTypeLabel = ""

		setTuIndiceTestContent {
			val mapping = rememberEvaluationItemMapping()

			quizLabel = EvaluationType.QUIZ.asString()
			mappedTypeLabel = mapping.typeLabel(EvaluationType.QUIZ)

			Text(text = quizLabel)
		}

		onNodeWithText(quizLabel).assertIsDisplayed()
		assertTrue(quizLabel.isNotBlank())
		assertTrue(mappedTypeLabel.isNotBlank())
		assertTrue(mappedTypeLabel == quizLabel)
	}

	@Test
	fun when_mappingRemembered_then_formatsCompletedPendingAndOverdueGrades() = runTuIndiceUiTest {
		var completedGrades = ""
		var pendingGrades = ""
		var overdueGrades = ""

		setTuIndiceTestContent {
			val mapping = rememberEvaluationItemMapping()

			completedGrades = mapping.gradesCompleted(18.5, 20.0)
			pendingGrades = mapping.gradesPending(20.0)
			overdueGrades = mapping.gradesOverdue(20.0)

			Text(text = pendingGrades)
		}

		onNodeWithText(pendingGrades).assertIsDisplayed()
		assertTrue(completedGrades.contains("18"))
		assertTrue(completedGrades.contains("20"))
		assertTrue(pendingGrades.contains("20"))
		assertTrue(overdueGrades.contains("20"))
		assertTrue(overdueGrades != pendingGrades)
	}
}
