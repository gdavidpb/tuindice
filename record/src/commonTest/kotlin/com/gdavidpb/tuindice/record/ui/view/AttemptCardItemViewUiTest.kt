package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.record.testing.attemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AttemptCardItemViewUiTest {
	@Test
	fun when_aSubjectIsDrawn_then_itsCardFramesTheSubject_andWhatItShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptCardItemView(
				item = attemptItem(attemptId = "attempt-1", subjectCode = "MA2115", isReadOnly = true),
				onGradeChange = { _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.attemptCard("attempt-1"))
		onNodeWithTag(RecordUiTags.attemptCard("attempt-1"))
			.assert(hasAnyDescendant(hasTestTag(RecordUiTags.attemptItem("attempt-1"))))
		onNodeWithText("MATERIA MA2115").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.attemptGradeValue("attempt-1", grade = 4)).assertTextEquals("4 / 5")

		// The card is the frame: the subject is drawn inside it, never past its edges.
		val card = onNodeWithTag(RecordUiTags.attemptCard("attempt-1")).getUnclippedBoundsInRoot()
		val item = onNodeWithTag(RecordUiTags.attemptItem("attempt-1")).getUnclippedBoundsInRoot()

		assertTrue(card.left <= item.left && item.right <= card.right, "the subject fits the card's width")
		assertTrue(card.top <= item.top && item.bottom <= card.bottom, "the subject fits the card's height")
	}

	@Test
	fun when_aSimulatedGradeIsHandedIn_then_theCardShowsIt_insteadOfTheStoredOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptCardItemView(
				item = attemptItem(attemptId = "attempt-1"),
				gradeState = remember { mutableIntStateOf(2) },
				onGradeChange = { _, _, _ -> }
			)
		}

		// The stored grade is 4; the list is simulating a 2.
		onNodeWithTag(RecordUiTags.attemptGradeValue("attempt-1", grade = 2)).assertTextEquals("2 / 5")
		assertNodeHidden(RecordUiTags.attemptGradeValue("attempt-1", grade = 4))
		assertNodeVisible(RecordUiTags.attemptGradeSlider("attempt-1"))
	}

	@Test
	fun when_aStatusIsChosenOnTheCard_then_theChoiceReachesTheCaller() = runTuIndiceUiTest {
		val reported = mutableListOf<Triple<Int?, AttemptOutcome?, Boolean>>()

		setTuIndiceTestContent {
			AttemptCardItemView(
				item = attemptItem(attemptId = "attempt-1", gradingMode = GradingMode.QUALITATIVE_PASS_FAIL),
				onGradeChange = { grade, outcome, isSelected -> reported += Triple(grade, outcome, isSelected) }
			)
		}

		onNodeWithTag(RecordUiTags.attemptStatusSelector("attempt-1")).performClick()
		onNodeWithTag(RecordUiTags.attemptStatusOption(attemptId = "attempt-1", status = "failed")).performClick()

		// No grade goes with a status, and choosing one commits it at once.
		assertEquals(listOf<Triple<Int?, AttemptOutcome?, Boolean>>(Triple(null, AttemptOutcome.FAILED, true)), reported)
	}
}
