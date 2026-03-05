package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.testing.sampleQuarterItem
import com.gdavidpb.tuindice.record.testing.sampleSubjectItem
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class QuartersViewUiTest {
	@Test
	fun when_quartersRendered_then_displaysListAndQuarterItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuartersView(
				lazyListState = rememberLazyListState(),
				quarters = listOf(
					sampleQuarterItem(
						quarterId = "quarter-1",
						subjects = listOf(
							sampleSubjectItem(
								subjectId = "subject-1",
								isReadOnly = false
							)
						)
					)
				),
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.QuartersList)
		assertNodeVisible(RecordUiTags.quarterItem(0))
		assertNodeVisible(RecordUiTags.subjectGradeSlider("subject-1"))
	}

	@Test
	fun when_quartersAreEmpty_then_displaysListWithoutQuarterRows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuartersView(
				lazyListState = rememberLazyListState(),
				quarters = emptyList(),
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeHidden(RecordUiTags.quarterItem(0))
	}

	@Test
	fun when_subjectSliderChangesInList_then_forwardsGradeChangeCallback() = runTuIndiceUiTest {
		val callbacks = mutableListOf<List<Any>>()

		setTuIndiceTestContent {
			QuartersView(
				lazyListState = rememberLazyListState(),
				quarters = listOf(
					sampleQuarterItem(
						quarterId = "quarter-list",
						subjects = listOf(
							sampleSubjectItem(
								subjectId = "subject-list",
								quarterId = "quarter-list",
								grade = 4,
								isReadOnly = false
							)
						)
					)
				),
				onSubjectGradeChange = { quarterId, subjectId, newGrade, isSelected ->
					callbacks += listOf(quarterId, subjectId, newGrade, isSelected)
				}
			)
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-list"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(5f))
			}

		waitForIdle()

		assertTrue(callbacks.isNotEmpty())
		assertTrue(
			callbacks.any { entry ->
				entry[0] == "quarter-list" &&
					entry[1] == "subject-list" &&
					entry[2] == 5 &&
					entry[3] == false
			}
		)
	}
}
