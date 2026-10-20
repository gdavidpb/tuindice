package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.ui.model.TuIndiceSubjectPointsBarTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TuIndiceSubjectPointsBarUiTest {
	@Test
	fun when_theEarnedIsWithinTheEvaluated_then_eachPartIsItsShareOfTheTotal() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSubjectPointsBar(
				subjectCode = "CI5311",
				earnedFraction = 0.25f,
				evaluatedFraction = 0.5f,
				modifier = Modifier.testTag(BAR_TAG),
				tags = TuIndiceSubjectPointsBarTags(earned = EARNED_TAG, evaluated = EVALUATED_TAG)
			)
		}
		waitForIdle()

		val track = onNodeWithTag(BAR_TAG).fetchSemanticsNode().size.width
		val earned = onNodeWithTag(EARNED_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width
		val evaluated = onNodeWithTag(EVALUATED_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width

		// The earned part is a quarter of the track, and the part evaluated and not earned is the next quarter.
		assertTrue(abs(earned - track * 0.25f) <= 1f, "the earned part is ${earned}px of a ${track}px track")
		assertTrue(abs(evaluated - track * 0.25f) <= 1f, "the evaluated part is ${evaluated}px of a ${track}px track")
	}

	@Test
	fun when_theEarnedIsMoreThanTheEvaluated_then_theEarnedPartIsCutToIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceSubjectPointsBar(
				subjectCode = "CI5311",
				earnedFraction = 0.8f,
				evaluatedFraction = 0.5f,
				modifier = Modifier.testTag(BAR_TAG),
				tags = TuIndiceSubjectPointsBarTags(earned = EARNED_TAG, evaluated = EVALUATED_TAG)
			)
		}
		waitForIdle()

		val track = onNodeWithTag(BAR_TAG).fetchSemanticsNode().size.width
		val earned = onNodeWithTag(EARNED_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width
		val evaluated = onNodeWithTag(EVALUATED_TAG, useUnmergedTree = true).fetchSemanticsNode().size.width

		assertTrue(abs(earned - track * 0.5f) <= 1f, "the earned part is ${earned}px of a ${track}px track")
		assertEquals(0, evaluated)
	}

	private companion object {
		const val BAR_TAG = "points_bar"
		const val EARNED_TAG = "points_bar_earned"
		const val EVALUATED_TAG = "points_bar_evaluated"
	}
}
