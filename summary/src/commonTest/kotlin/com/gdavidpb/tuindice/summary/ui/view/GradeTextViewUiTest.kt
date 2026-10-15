package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
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
		onNodeWithTag(SummaryUiTags.GradeText).assertTextContains("4.2500 / 5.0")
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
		onNodeWithTag(SummaryUiTags.GradeText).assertTextContains("8.5000 / 10.0")
	}

	@Test
	fun when_animationsAreOff_then_theGradeIsWrittenAtOnce() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				GradeTextView(grade = 4.25f, maxGrade = 5f)
			}
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.GradeText).assertTextContains("4.2500 / 5.0")
	}

	@Test
	fun when_aStyleAndATagAreGiven_then_theGradeIsWrittenUnderThatTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				GradeTextView(
					grade = 4.125f,
					style = MaterialTheme.typography.titleLarge,
					tag = CUSTOM_TAG
				)
			}
		}
		waitForIdle()

		assertNodeVisible(CUSTOM_TAG)
		onNodeWithTag(CUSTOM_TAG).assertTextContains("4.1250 / 5.0")
	}

	private companion object {
		const val CUSTOM_TAG = "summary_grade_text_custom"
	}
}
