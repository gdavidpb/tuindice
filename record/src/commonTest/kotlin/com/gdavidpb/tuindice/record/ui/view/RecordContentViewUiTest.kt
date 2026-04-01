package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_SUBJECT
import com.gdavidpb.tuindice.record.testing.recordContentState
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordContentViewUiTest {
	private data class GradeChangeEvent(
		val quarterId: String,
		val subjectId: String,
		val grade: Int,
		val isSelected: Boolean
	)

	@Test
	fun when_contentViewIsRendered_then_displaysQuarterSelectorAndSelectedQuarter() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordContentView(
				state = recordContentState(),
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.ContentContainer)
		assertNodeVisible(RecordUiTags.QuarterSelectorRow)
		assertNodeVisible(RecordUiTags.quarterChip("quarter-1"))
		assertNodeVisible(
			tag = RecordUiTags.quarterCurrentChip("quarter-1"),
			useUnmergedTree = true
		)
		assertNodeVisible(RecordUiTags.SelectedQuarterSummary)
		assertNodeVisible(RecordUiTags.subjectItem("subject-1"))
		onAllNodesWithText("2026-1").assertCountEquals(1)
		onAllNodesWithText("Actual").assertCountEquals(0)
	}

	@Test
	fun when_contentStateHasNoQuarters_then_showsContainerWithoutQuarterSelectorOrSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordContentView(
				state = recordContentState(quarters = emptyList()),
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeHidden(RecordUiTags.QuarterSelectorRow)
		assertNodeHidden(RecordUiTags.SelectedQuarterSummary)
		assertNodeHidden(RecordUiTags.subjectItem("subject-1"))
	}

	@Test
	fun when_subjectGradeSliderChanges_then_forwardsGradeChangeToCallback() = runTuIndiceUiTest {
		val events = mutableListOf<GradeChangeEvent>()
		val state = recordContentState(
			quarters = listOf(
				DEFAULT_RECORD_QUARTER.copy(
					grade = 4.0,
					gradeSum = 4.0,
					subjects = listOf(
						DEFAULT_RECORD_SUBJECT.copy(grade = 4)
					)
				)
			)
		)

		setTuIndiceTestContent {
			RecordContentView(
				state = state,
				selectedQuarterId = "quarter-1",
				onSelectedQuarterChange = {},
				onSubjectGradeChange = { quarterId, subjectId, newGrade, isSelected ->
					events += GradeChangeEvent(quarterId, subjectId, newGrade, isSelected)
				}
			)
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-1"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(5f))
			}

		waitForIdle()

		assertTrue(events.isNotEmpty())
		assertTrue(
			events.any { event ->
				event.quarterId == "quarter-1" &&
					event.subjectId == "subject-1" &&
					event.grade == 5 &&
					!event.isSelected
			}
		)
	}

	@Test
	fun when_quarterChipIsTapped_then_displaysSubjectsForThatQuarter() = runTuIndiceUiTest {
		val olderQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-2",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			subjects = listOf(
				DEFAULT_RECORD_SUBJECT.copy(
					id = "subject-2",
					quarterId = "quarter-2",
					name = "Calculo"
				)
			)
		)

		setTuIndiceTestContent {
			val selectedQuarterIdState = remember {
				mutableStateOf<String?>(DEFAULT_RECORD_QUARTER.id)
			}

			RecordContentView(
				state = recordContentState(
					quarters = listOf(
						DEFAULT_RECORD_QUARTER,
						olderQuarter
					)
				),
				selectedQuarterId = selectedQuarterIdState.value,
				onSelectedQuarterChange = { quarterId ->
					selectedQuarterIdState.value = quarterId
				},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.subjectItem("subject-1"))
		assertNodeHidden(RecordUiTags.subjectItem("subject-2"))

		onNodeWithTag(RecordUiTags.quarterChip("quarter-2")).performClick()
		waitForIdle()

		assertNodeHidden(RecordUiTags.subjectItem("subject-1"))
		assertNodeVisible(RecordUiTags.subjectItem("subject-2"))
		onAllNodesWithText("Actual").assertCountEquals(0)
		assertNodeHidden(
			tag = RecordUiTags.quarterCurrentChip("quarter-2"),
			useUnmergedTree = true
		)
	}

	@Test
	fun when_quarterSelectorRowIsSwiped_then_selectedQuarterDoesNotChange() = runTuIndiceUiTest {
		val olderQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-2",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			subjects = listOf(
				DEFAULT_RECORD_SUBJECT.copy(
					id = "subject-2",
					quarterId = "quarter-2",
					name = "Calculo"
				)
			)
		)

		setTuIndiceTestContent {
			val selectedQuarterIdState = remember {
				mutableStateOf<String?>(DEFAULT_RECORD_QUARTER.id)
			}

			RecordContentView(
				state = recordContentState(
					quarters = listOf(
						DEFAULT_RECORD_QUARTER,
						olderQuarter
					)
				),
				selectedQuarterId = selectedQuarterIdState.value,
				onSelectedQuarterChange = { quarterId ->
					selectedQuarterIdState.value = quarterId
				},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		onNodeWithTag(RecordUiTags.QuarterSelectorRow).performTouchInput {
			swipeLeft()
		}
		waitForIdle()

		assertNodeVisible(RecordUiTags.subjectItem("subject-1"))
		assertNodeHidden(RecordUiTags.subjectItem("subject-2"))
	}

	@Test
	fun when_quarterPagerIsSwiped_then_displaysSubjectsForAdjacentQuarter() = runTuIndiceUiTest {
		val olderQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-2",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			subjects = listOf(
				DEFAULT_RECORD_SUBJECT.copy(
					id = "subject-2",
					quarterId = "quarter-2",
					name = "Calculo"
				)
			)
		)

		setTuIndiceTestContent {
			val selectedQuarterIdState = remember {
				mutableStateOf<String?>(DEFAULT_RECORD_QUARTER.id)
			}

			RecordContentView(
				state = recordContentState(
					quarters = listOf(
						DEFAULT_RECORD_QUARTER,
						olderQuarter
					)
				),
				selectedQuarterId = selectedQuarterIdState.value,
				onSelectedQuarterChange = { quarterId ->
					selectedQuarterIdState.value = quarterId
				},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.subjectItem("subject-1"))
		assertNodeHidden(RecordUiTags.subjectItem("subject-2"))

		onNodeWithTag(RecordUiTags.QuarterPager).performTouchInput {
			swipeRight()
		}
		waitForIdle()

		assertNodeHidden(RecordUiTags.subjectItem("subject-1"))
		assertNodeVisible(RecordUiTags.subjectItem("subject-2"))
	}

	@Test
	fun when_quarterPagerIsSwiped_then_notifiesSelectedQuarterChange() = runTuIndiceUiTest {
		val olderQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-2",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			subjects = listOf(
				DEFAULT_RECORD_SUBJECT.copy(
					id = "subject-2",
					quarterId = "quarter-2",
					name = "Calculo"
				)
			)
		)
		val selectedQuarterChanges = mutableListOf<String>()

		setTuIndiceTestContent {
			val selectedQuarterIdState = remember {
				mutableStateOf<String?>(DEFAULT_RECORD_QUARTER.id)
			}

			RecordContentView(
				state = recordContentState(
					quarters = listOf(
						DEFAULT_RECORD_QUARTER,
						olderQuarter
					)
				),
				selectedQuarterId = selectedQuarterIdState.value,
				onSelectedQuarterChange = { quarterId ->
					selectedQuarterChanges += quarterId
					selectedQuarterIdState.value = quarterId
				},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		onNodeWithTag(RecordUiTags.QuarterPager).performTouchInput {
			swipeRight()
		}

		waitUntil(timeoutMillis = 2_000) {
			selectedQuarterChanges.contains("quarter-2")
		}

		assertTrue(selectedQuarterChanges.contains("quarter-2"))
	}
}
