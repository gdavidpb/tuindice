package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class GradeTextViewUiTest {
	@Test
	fun when_gradeAnimationCompletes_then_displaysFormattedGrade() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradeTextView(
				grade = 4.25f,
				maxGrade = 5f
			)
		}

		advanceAnimationsBy(1_000)

		assertNodeVisible(SummaryUiTags.GradeText)
		onNodeWithTag(SummaryUiTags.GradeText).assertTextContains("4.2500/5.0")
	}

	@Test
	fun when_customMaxGradeProvided_then_displaysCustomScale() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			GradeTextView(
				grade = 8.5f,
				maxGrade = 10f
			)
		}

		advanceAnimationsBy(1_000)

		assertNodeVisible(SummaryUiTags.GradeText)
		onNodeWithTag(SummaryUiTags.GradeText).assertTextContains("8.5000/10.0")
	}
}
