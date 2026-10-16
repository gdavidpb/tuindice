package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryEvaluatedBarViewUiTest {
	@Test
	fun when_nothingIsEvaluated_then_neitherPartHasWidth() = runTuIndiceUiTest {
		renderBar(earned = 0f, evaluated = 0f)

		assertParts(earned = 0f, evaluated = 0f)
	}

	@Test
	fun when_nothingIsEarned_then_theEvaluatedPartIsItsWholeShare() = runTuIndiceUiTest {
		renderBar(earned = 0f, evaluated = 0.5f)

		assertParts(earned = 0f, evaluated = 0.5f)
	}

	@Test
	fun when_someIsEarned_then_theEarnedPartIsFromTheStart_andTheRestOfTheEvaluatedFollowsIt() = runTuIndiceUiTest {
		renderBar(earned = 0.3f, evaluated = 0.6f)

		assertParts(earned = 0.3f, evaluated = 0.6f)
	}

	@Test
	fun when_earnedIsMoreThanEvaluated_then_theEarnedPartStopsAtWhatIsEvaluated() = runTuIndiceUiTest {
		renderBar(earned = 0.8f, evaluated = 0.5f)

		assertParts(earned = 0.5f, evaluated = 0.5f)
	}

	@Test
	fun when_fractionsArePastTheTotal_then_theBarIsWhole() = runTuIndiceUiTest {
		renderBar(earned = 1.2f, evaluated = 1.4f)

		assertParts(earned = 1f, evaluated = 1f)
	}

	@Test
	fun when_described_then_theBarSaysItsDescriptionToTheReader() = runTuIndiceUiTest {
		renderBar(earned = 0.1f, evaluated = 0.6f, description = "60 de 100 evaluados")
		waitForIdle()

		onNodeWithTag(SummaryUiTags.termPointsBar(SUBJECT_CODE))
			.assertContentDescriptionEquals("60 de 100 evaluados")
	}

	@Test
	fun when_notDescribed_then_theBarSaysNothingToTheReader() = runTuIndiceUiTest {
		renderBar(earned = 0f, evaluated = 0f, description = null)
		waitForIdle()

		onNodeWithTag(SummaryUiTags.termPointsBar(SUBJECT_CODE)).assert(
			SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription)
		)
	}

	@Test
	fun when_rendered_then_itIsTheSharedHeight() = runTuIndiceUiTest {
		renderBar(earned = 0.3f, evaluated = 0.5f)
		waitForIdle()

		onNodeWithTag(SummaryUiTags.termPointsBar(SUBJECT_CODE))
			.assertHeightIsEqualTo(TuIndiceComponentSizes.ProgressBarHeight)
	}

	private fun ComposeUiTest.renderBar(earned: Float, evaluated: Float, description: String? = null) {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(TRACK_WIDTH)) {
				SummaryEvaluatedBarView(
					subjectCode = SUBJECT_CODE,
					earnedFraction = earned,
					evaluatedFraction = evaluated,
					description = description
				)
			}
		}
	}

	// The earned part starts the track, and the evaluated part that is not earned yet starts where the earned one
	// ends. Each is as wide as its share of the track.
	private fun ComposeUiTest.assertParts(earned: Float, evaluated: Float) {
		waitForIdle()

		val track = onNodeWithTag(SummaryUiTags.termPointsBar(SUBJECT_CODE), useUnmergedTree = true)
			.fetchSemanticsNode().boundsInRoot
		val earnedPart = onNodeWithTag(SummaryEvaluatedBarUiTags.earned(SUBJECT_CODE), useUnmergedTree = true)
			.fetchSemanticsNode().boundsInRoot
		val restPart = onNodeWithTag(SummaryUiTags.termPointsBarFill(SUBJECT_CODE), useUnmergedTree = true)
			.fetchSemanticsNode().boundsInRoot

		assertTrue(abs(earnedPart.left - track.left) <= 1f, "the earned part starts the track")
		assertTrue(
			abs(earnedPart.width - track.width * earned) <= 1f,
			"the earned part is ${earnedPart.width}px of a track of ${track.width}px, not $earned of it"
		)
		// A part with no width is not drawn, so there is no place to check for it.
		if (restPart.width > 1f) {
			assertTrue(abs(restPart.left - earnedPart.right) <= 1f, "the evaluated part starts where the earned one ends")
		}
		assertTrue(
			abs(restPart.width - track.width * (evaluated - earned)) <= 1f,
			"the evaluated part is ${restPart.width}px of a track of ${track.width}px, not ${evaluated - earned} of it"
		)
	}

	private companion object {
		const val SUBJECT_CODE = "CI5311"
		val TRACK_WIDTH = 200.dp
	}
}
