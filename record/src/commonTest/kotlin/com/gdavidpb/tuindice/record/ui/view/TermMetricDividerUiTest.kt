package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// The divider is a painted line with no semantics of its own, so it is measured through what it
// does to the row around it. Its colour is drawing only and is not asserted.
@OptIn(ExperimentalTestApi::class)
class TermMetricDividerUiTest {
	@Test
	fun when_theDividerStandsAlone_then_itIsOneDpWide_andAsTallAsTheRowLetsIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row(
				modifier = Modifier
					.height(40.dp)
					.testTag(HostTag)
			) {
				TermMetricDivider()
			}
		}

		// The row wraps what it holds: its width is the divider's.
		onNodeWithTag(HostTag)
			.assertWidthIsEqualTo(1.dp)
			.assertHeightIsEqualTo(40.dp)
	}

	@Test
	fun when_theDividerSeparatesTwoMetrics_then_itTakesOneDpBetweenThem_withoutStretchingTheRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row(
				modifier = Modifier
					.height(IntrinsicSize.Min)
					.testTag(HostTag)
			) {
				Text(modifier = Modifier.testTag(LeftTag), text = "4.0000")
				TermMetricDivider()
				Text(modifier = Modifier.testTag(RightTag), text = "3.5000")
			}
		}

		val host = onNodeWithTag(HostTag).getUnclippedBoundsInRoot()
		val left = onNodeWithTag(LeftTag).getUnclippedBoundsInRoot()
		val right = onNodeWithTag(RightTag).getUnclippedBoundsInRoot()

		assertDpEquals(expected = 1.dp, actual = right.left - left.right, what = "the gap the divider takes")
		// It fills the height it is given but asks for none: the metrics decide how tall the row is.
		assertDpEquals(expected = left.bottom - left.top, actual = host.bottom - host.top, what = "the row's height")
	}

	private companion object {
		const val HostTag = "divider_host"
		const val LeftTag = "divider_left"
		const val RightTag = "divider_right"
	}
}
