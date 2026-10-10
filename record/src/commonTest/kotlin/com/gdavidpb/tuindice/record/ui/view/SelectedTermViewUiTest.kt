package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.record.testing.attemptItem
import com.gdavidpb.tuindice.record.testing.termItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SelectedTermViewUiTest {
	@Test
	fun when_theTermHasSubjects_then_eachHasItsCard_inTheOrderOfTheTerm() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SelectedTermView(
				term = termItem(
					attempts = listOf(
						attemptItem(attemptId = "attempt-1", subjectCode = "MA2115", isReadOnly = true),
						attemptItem(attemptId = "attempt-2", subjectCode = "FS2211", isReadOnly = true)
					)
				),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.AttemptsList)
		onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-1")).assertTextEquals("MA2115")
		onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-2")).assertTextEquals("FS2211")

		val first = onNodeWithTag(RecordUiTags.attemptCard("attempt-1")).assertIsDisplayed().getUnclippedBoundsInRoot()
		val second = onNodeWithTag(RecordUiTags.attemptCard("attempt-2")).assertIsDisplayed().getUnclippedBoundsInRoot()

		assertTrue(first.bottom <= second.top, "the cards follow the order of the term")
		// A closed term is only read: none of its subjects offers a grade to move.
		assertNodeHidden(RecordUiTags.attemptGradeSlider("attempt-1"))
		assertNodeHidden(RecordUiTags.attemptGradeSlider("attempt-2"))
	}

	@Test
	fun when_theTermHasNoSubjects_then_theListIsThere_andHoldsNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// Sized by its page, as in the record: with nothing to hold, the list has no width of its own.
			SelectedTermView(
				modifier = Modifier.fillMaxSize(),
				term = termItem(attempts = emptyList()),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.AttemptsList)
		onNodeWithTag(RecordUiTags.AttemptsList).onChildren().assertCountEquals(0)
		onAllNodes(hasClickAction()).assertCountEquals(0)
	}

	@Test
	fun when_aGradeIsSimulated_then_itIsReportedWithItsSubject_whileMoving_andOnceSettled() = runTuIndiceUiTest {
		val reported = mutableListOf<Selection>()

		setTuIndiceTestContent {
			SelectedTermView(
				term = termItem(
					attempts = listOf(
						attemptItem(attemptId = "attempt-1", subjectCode = "MA2115"),
						attemptItem(attemptId = "attempt-2", subjectCode = "FS2211")
					)
				),
				onAttemptSelectionChange = { attemptId, grade, outcome, isSelected ->
					reported += Selection(attemptId, grade, outcome, isSelected)
				}
			)
		}

		onNodeWithTag(RecordUiTags.attemptGradeValue("attempt-2", grade = 4)).assertTextEquals("4 / 5")
		onNodeWithTag(RecordUiTags.attemptGradeSlider("attempt-2"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress -> setProgress(2f) }
		waitForIdle()

		// The move is a draft; letting go is what commits it. Both name the subject that was moved.
		assertEquals(
			expected = listOf(
				Selection(attemptId = "attempt-2", grade = 2, outcome = null, isSelected = false),
				Selection(attemptId = "attempt-2", grade = 2, outcome = null, isSelected = true)
			),
			actual = reported
		)
		// The list keeps the grade being simulated: the card shows it before any save comes back.
		onNodeWithTag(RecordUiTags.attemptGradeValue("attempt-2", grade = 2)).assertTextEquals("2 / 5")
		// And only that subject moved.
		onNodeWithTag(RecordUiTags.attemptGradeValue("attempt-1", grade = 4)).assertTextEquals("4 / 5")
	}

	@Test
	fun when_aQualitativeStatusIsChosen_then_itIsReportedWithItsSubject_andNoGrade() = runTuIndiceUiTest {
		val reported = mutableListOf<Selection>()

		setTuIndiceTestContent {
			SelectedTermView(
				term = termItem(
					attempts = listOf(
						attemptItem(
							attemptId = "attempt-9",
							subjectCode = "EP5406",
							gradingMode = GradingMode.QUALITATIVE_PASS_FAIL
						)
					)
				),
				onAttemptSelectionChange = { attemptId, grade, outcome, isSelected ->
					reported += Selection(attemptId, grade, outcome, isSelected)
				}
			)
		}

		// A subject graded pass/fail has no slider: its status is chosen from a list.
		assertNodeHidden(RecordUiTags.attemptGradeSlider("attempt-9"))
		onNodeWithTag(RecordUiTags.attemptStatusSelector("attempt-9")).performClick()
		onNodeWithTag(RecordUiTags.attemptStatusOption(attemptId = "attempt-9", status = "approved")).performClick()

		assertEquals(
			expected = listOf(
				Selection(attemptId = "attempt-9", grade = null, outcome = AttemptOutcome.APPROVED, isSelected = true)
			),
			actual = reported
		)
	}

	private data class Selection(
		val attemptId: String,
		val grade: Int?,
		val outcome: AttemptOutcome?,
		val isSelected: Boolean
	)
}
