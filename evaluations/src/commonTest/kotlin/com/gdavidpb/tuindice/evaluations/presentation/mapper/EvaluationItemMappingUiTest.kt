package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

	@Test
	fun toEvaluationItemList_mapsRequiredSubjectNameGradeAndStatus() = runTest {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			grade = 32.0,
			maxGrade = 35.0,
			state = EvaluationState.COMPLETED
		)
		val item = listOf(evaluation).toEvaluationItemList(
			mapping = getEvaluationItemMapping(),
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT)
		).single().items.single()

		assertEquals(DEFAULT_EVALUATION_SUBJECT.name, item.subjectNameText)
		assertEquals("32 / 35", item.gradeText)
		assertEquals("Completada", item.statusText)
		assertTrue(item.showsGradeAction)
		assertTrue(item.isClickable)
	}

	@Test
	fun toEvaluationItemList_failsWhenLocalAttemptIsMissing() = runTest {
		val error = assertFailsWith<IllegalStateException> {
			listOf(DEFAULT_PENDING_EVALUATION).toEvaluationItemList(
				mapping = getEvaluationItemMapping(),
				attempts = emptyList()
			)
		}

		assertTrue(error.message.orEmpty().contains(DEFAULT_PENDING_EVALUATION.attemptId))
	}
}
