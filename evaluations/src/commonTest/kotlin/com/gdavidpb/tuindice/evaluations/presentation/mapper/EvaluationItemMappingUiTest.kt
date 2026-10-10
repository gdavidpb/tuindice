package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationItemMappingUiTest {
	@Test
	fun when_typeIsRendered_then_itsLabelIsTheOneTheMappingGivesTheItems() = runTuIndiceUiTest {
		val mapping = getEvaluationItemMapping()
		val expectedLabels = listOf(
			EvaluationType.TEST to "Parcial",
			EvaluationType.QUIZ to "Quiz",
			EvaluationType.WRITTEN_WORK to "Trabajo escrito",
			EvaluationType.OTHER to "Otra"
		)

		setTuIndiceTestContent {
			Column {
				expectedLabels.forEach { (type, _) ->
					Text(
						modifier = Modifier.testTag(typeLabelTag(type)),
						text = type.asString()
					)
				}
			}
		}

		expectedLabels.forEach { (type, label) ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(typeLabelTag(type)).assertTextEquals(label)
			assertEquals(label, mapping.typeLabel(type))
		}
	}

	@Test
	fun when_gradesAreRendered_then_eachStateReadsItsOwnPattern() = runTuIndiceUiTest {
		val mapping = getEvaluationItemMapping()

		setTuIndiceTestContent {
			Column {
				Text(
					modifier = Modifier.testTag(COMPLETED_GRADES_TAG),
					text = mapping.gradesCompleted(18.5, 20.0)
				)
				Text(
					modifier = Modifier.testTag(UNGRADED_COMPLETED_GRADES_TAG),
					text = mapping.gradesCompleted(null, 20.0)
				)
				Text(
					modifier = Modifier.testTag(PENDING_GRADES_TAG),
					text = mapping.gradesPending(20.0)
				)
				Text(
					modifier = Modifier.testTag(OVERDUE_GRADES_TAG),
					text = mapping.gradesOverdue(20.0)
				)
			}
		}

		onNodeWithTag(COMPLETED_GRADES_TAG).assertTextEquals("18.50 / 20.00")
		onNodeWithTag(UNGRADED_COMPLETED_GRADES_TAG).assertTextEquals("0.00 / 20.00")
		onNodeWithTag(PENDING_GRADES_TAG).assertTextEquals("Pendiente / 20.00")
		onNodeWithTag(OVERDUE_GRADES_TAG).assertTextEquals("Sin nota / 20.00")
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

		assertEquals("QUIZ 1", item.nameText)
		assertEquals("QUIZ 1", item.typeNameText)
		assertEquals("Quiz", item.typeText)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.name, item.subjectNameText)
		assertEquals("32 / 35", item.gradeText)
		assertEquals("Completada", item.statusText)
		assertTrue(item.showsGradeAction)
		assertTrue(item.isClickable)
	}

	// It used to throw here, and the throw closed the app from the machine's job.
	@Test
	fun toEvaluationItemList_leavesOutAnEvaluationWhenItsLocalAttemptIsMissing() = runTest {
		val groups = listOf(DEFAULT_PENDING_EVALUATION).toEvaluationItemList(
			mapping = getEvaluationItemMapping(),
			attempts = emptyList()
		)

		assertEquals(emptyList(), groups)
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
		const val COMPLETED_GRADES_TAG = "evaluation_item_mapping_completed_grades"
		const val UNGRADED_COMPLETED_GRADES_TAG = "evaluation_item_mapping_ungraded_completed_grades"
		const val PENDING_GRADES_TAG = "evaluation_item_mapping_pending_grades"
		const val OVERDUE_GRADES_TAG = "evaluation_item_mapping_overdue_grades"

		fun typeLabelTag(type: EvaluationType) = "evaluation_item_mapping_type_label_${type.name}"
	}
}
