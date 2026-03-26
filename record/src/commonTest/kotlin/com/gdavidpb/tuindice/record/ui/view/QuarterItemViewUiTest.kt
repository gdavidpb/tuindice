package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.testing.sampleQuarterItem
import com.gdavidpb.tuindice.record.testing.sampleSubjectItem
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class QuarterItemViewUiTest {
	@Test
	fun when_quarterItemIsRendered_then_displaysMetrics() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuarterItemView(
				item = sampleQuarterItem(),
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		onNodeWithText("Δx 4.2500").assertIsDisplayed()
		onNodeWithText("∑x 4.2500").assertIsDisplayed()
		onNodeWithText("⦿ 6").assertIsDisplayed()
	}

	@Test
	fun when_subjectSliderChanges_then_forwardsQuarterAndSubjectIdentifiers() = runTuIndiceUiTest {
		val callbacks = mutableListOf<List<Any>>()

		setTuIndiceTestContent {
			QuarterItemView(
				item = sampleQuarterItem(
					quarterId = "quarter-callback",
					subjects = listOf(
						sampleSubjectItem(
							subjectId = "subject-callback",
							quarterId = "quarter-callback",
							grade = 4,
							isReadOnly = false
						)
					)
				),
				onSubjectGradeChange = { quarterId, subjectId, grade, isSelected ->
					callbacks += listOf(quarterId, subjectId, grade, isSelected)
				}
			)
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-callback"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(3f))
			}

		waitForIdle()

		assertTrue(callbacks.isNotEmpty())
		assertTrue(
			callbacks.any { entry ->
				entry[0] == "quarter-callback" &&
					entry[1] == "subject-callback" &&
					entry[2] == 3 &&
					entry[3] == false
			}
		)
	}

	@Test
	fun when_allSubjectsAreReadOnly_then_hidesSubjectSliders() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuarterItemView(
				item = sampleQuarterItem(
					quarterId = "quarter-read-only",
					subjects = listOf(
						sampleSubjectItem(
							subjectId = "subject-read-only",
							quarterId = "quarter-read-only",
							isReadOnly = true
						)
					)
				),
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		assertNodeHidden(RecordUiTags.subjectGradeSlider("subject-read-only"))
	}

	@Test
	fun when_subjectGradeChangesRemotely_then_resyncsDisplayedSliderValue() = runTuIndiceUiTest {
		val itemState = mutableStateOf(
			sampleQuarterItem(
				quarterId = "quarter-sync",
				subjects = listOf(
					sampleSubjectItem(
						subjectId = "subject-sync",
						quarterId = "quarter-sync",
						grade = 4,
						isReadOnly = false
					)
				)
			)
		)

		setTuIndiceTestContent {
			QuarterItemView(
				item = itemState.value,
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-sync"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(2f))
			}

		waitForIdle()
		onNodeWithText("2 / 5").assertIsDisplayed()

		runOnIdle {
			itemState.value = sampleQuarterItem(
				quarterId = "quarter-sync",
				subjects = listOf(
					sampleSubjectItem(
						subjectId = "subject-sync",
						quarterId = "quarter-sync",
						grade = 5,
						isReadOnly = false
					)
				)
			)
		}

		waitForIdle()
		onNodeWithText("5 / 5").assertIsDisplayed()
	}
}
