package com.gdavidpb.tuindice.record.ui.view

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
	fun when_quarterItemIsRendered_then_displaysHeaderAndMetrics() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuarterItemView(
				item = sampleQuarterItem(),
				onSubjectGradeChange = { _, _, _, _ -> }
			)
		}

		onNodeWithText("Abril - Julio 2023").assertIsDisplayed()
		onNodeWithText("Δx 4.2500").assertIsDisplayed()
		onNodeWithText("∑x 4.2500").assertIsDisplayed()
		onNodeWithText("⦿ 6 UC").assertIsDisplayed()
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
}
