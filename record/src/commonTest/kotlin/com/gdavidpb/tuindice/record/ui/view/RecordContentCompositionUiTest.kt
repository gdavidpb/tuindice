package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.ui.model.RecordActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names the parts that compose: the record, and each card of a term. A number that animates must compose
// none of them for its frames, only once for the data that started it.
@OptIn(ExperimentalTestApi::class)
class RecordContentCompositionUiTest {
	@Test
	fun when_aTermsAverageChanges_then_itsNumberAnimatesWithoutRecomposingTheRecordOrItsCard() = runTuIndiceUiTest {
		val compositions = mutableListOf<String>()
		val probe: (String) -> Unit = { name -> compositions += name }
		var grade by mutableStateOf(4)
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				Content(
					state = contentState(
						terms = listOf(historicalTerm("term-b", 2026, grade)),
						selectedTermId = "term-b"
					)
				)
			}
		}

		// The screen's own start, with its first frame, is counted before the change: it is not part of it.
		waitForIdle()
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val settled = compositions.countsByName()

		// The average falls from 4 to 2. The new data is composed on the first frame after the change, which also starts
		// the animation: the record and its card compose once each for it, no more.
		grade = 2
		mainClock.advanceTimeByFrame()
		waitForIdle()
		val changed = compositions.countsByName()
		assertTrue(
			changed.getValue("record-content") - settled.getValue("record-content") <= 1,
			"the record composes once for the new data"
		)
		assertTrue(
			changed.getValue("term-card:attempt-term-b") - settled.getValue("term-card:attempt-term-b") <= 1,
			"the card composes once for the new data"
		)

		// The number moves over the frames, and none of those frames composes the record or the card.
		repeat(4) { mainClock.advanceTimeByFrame() }
		waitForIdle()
		val fiveFrames = compositions.countsByName()
		onAllNodesWithText("Δx 4.0000").assertCountEquals(0)
		repeat(60) { mainClock.advanceTimeByFrame() }
		waitForIdle()
		val animated = compositions.countsByName()

		assertEquals(changed, fiveFrames, "frames two to five compose neither the record nor the card")
		assertEquals(fiveFrames, animated, "the rest of the animation composes neither the record nor the card")
		onNodeWithText("Δx 2.0000").assertIsDisplayed()
	}

	@Test
	fun when_anOlderTermLeaves_then_theTermThatMovesUpKeepsItsOwnNumber() = runTuIndiceUiTest {
		var terms by mutableStateOf(
			listOf(historicalTerm("term-a", 2025, grade = 2), historicalTerm("term-b", 2026, grade = 4))
		)
		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			Content(state = contentState(terms = terms, selectedTermId = "term-b"))
		}

		waitForIdle()
		onNodeWithText("Δx 4.0000").assertIsDisplayed()

		// The older term goes, and term-b moves into the place it held. Its number starts at its own value, and does
		// not count up from the 2 that place's animation was holding.
		terms = listOf(historicalTerm("term-b", 2026, grade = 4))
		waitForIdle()
		onNodeWithText("Δx 4.0000").assertIsDisplayed()
	}

	@Composable
	private fun Content(state: Record.State.Content) {
		RecordContentView(
			state = state,
			selectedTermId = state.selectedTermId,
			actions = RecordActions(
				onSelectedTermChange = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onDismissTermSelection = {}
			),
			showTermSelection = false
		)
	}

	private fun contentState(
		terms: List<AcademicTerm>,
		selectedTermId: String
	) = Record.State.Content(
		viewMode = RecordViewMode.Historical,
		record = AcademicRecord(id = "record", terms = terms),
		selectedTermId = selectedTermId
	)

	// A term with one subject of four credits, graded as given: its average is that grade.
	private fun historicalTerm(
		id: String,
		periodYear: Int,
		grade: Int
	) = academicTerm(
		id = id,
		kind = TermKind.HISTORICAL,
		periodYear = periodYear,
		attempts = listOf(
			academicAttempt(
				subjectCode = "MA1112",
				id = "attempt-$id",
				score = AttemptScore.numeric(grade)
			)
		)
	)

	private fun List<String>.countsByName() = groupingBy { it }.eachCount()
}
