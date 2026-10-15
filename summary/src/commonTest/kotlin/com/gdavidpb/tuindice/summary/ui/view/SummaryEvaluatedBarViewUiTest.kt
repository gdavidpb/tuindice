package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
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
	fun when_fractionIsAHalf_then_theFillIsHalfOfTheTrack() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(200.dp)) {
				SummaryEvaluatedBarView(subjectCode = "CI5311", fraction = 0.5f)
			}
		}

		assertFillShare(expected = 0.5f)
	}

	@Test
	fun when_fractionIsMoreThanTheTotal_then_theFillIsTheWholeTrack() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(200.dp)) {
				SummaryEvaluatedBarView(subjectCode = "CI5311", fraction = 1.4f)
			}
		}

		assertFillShare(expected = 1f)
	}

	@Test
	fun when_nothingIsEvaluated_then_theFillHasNoWidth() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(200.dp)) {
				SummaryEvaluatedBarView(subjectCode = "CI5311", fraction = 0f)
			}
		}

		assertFillShare(expected = 0f)
	}

	@Test
	fun when_rendered_then_itIsTheSharedHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(200.dp)) {
				SummaryEvaluatedBarView(subjectCode = "CI5311", fraction = 0.5f)
			}
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.termPointsBar("CI5311"))
			.assertHeightIsEqualTo(TuIndiceComponentSizes.ProgressBarHeight)
	}

	private fun ComposeUiTest.assertFillShare(expected: Float) {
		val track = onNodeWithTag(SummaryUiTags.termPointsBar("CI5311")).fetchSemanticsNode().size.width
		val fill = onNodeWithTag(SummaryUiTags.termPointsBarFill("CI5311")).fetchSemanticsNode().size.width

		assertTrue(
			abs(fill - track * expected) <= 1f,
			"the fill is $fill px of a track of $track px, not $expected of it"
		)
	}
}
