package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_SUBJECT
import com.gdavidpb.tuindice.record.testing.recordContentState
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordScreenUiTest {
	@Test
	fun when_stateIsLoading_then_displaysLoadingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = Record.State.Loading,
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onRetryClick = {},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.LoadingIndicator)
	}

	@Test
	fun when_stateIsFailedAndRetryTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			RecordScreen(
				state = Record.State.Failed,
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onRetryClick = { retryClicks++ },
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryClicks)
	}

	@Test
	fun when_stateIsEmpty_then_displaysEmptyView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = Record.State.Empty,
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onRetryClick = {},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.EmptyContainer)
	}

	@Test
	fun when_stateIsContent_then_displaysRecordContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = recordContentState(),
				selectedQuarterId = null,
				onSelectedQuarterChange = {},
				onRetryClick = {},
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.ContentContainer)
	}

	@Test
	fun when_subjectGradeChangesInContent_then_invokesSubjectGradeCallback() = runTuIndiceUiTest {
		val events = mutableListOf<List<Any>>()
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
			RecordScreen(
				state = state,
				selectedQuarterId = "quarter-1",
				onSelectedQuarterChange = {},
				onRetryClick = {},
				onSubjectGradeChange = { quarterId, subjectId, newGrade, isSelected ->
					events += listOf(quarterId, subjectId, newGrade, isSelected)
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
				event[0] == "quarter-1" &&
					event[1] == "subject-1" &&
					event[2] == 5 &&
					event[3] == false
			}
		)
	}
}
