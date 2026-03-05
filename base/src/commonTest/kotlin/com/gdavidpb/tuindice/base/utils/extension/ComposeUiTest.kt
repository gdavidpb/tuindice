package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ComposeUiTest {
	@Test
	fun when_measureUnconstrainedViewSize_then_reportsMeasuredDimensions() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			MeasureUnconstrainedViewSize(
				viewToMeasure = {
					Box(modifier = Modifier.size(width = 40.dp, height = 20.dp))
				}
			) { measuredSize ->
				Text("${measuredSize.width.value.toInt()}x${measuredSize.height.value.toInt()}")
			}
		}

		onNodeWithText("40x20").assertIsDisplayed()
	}

	@Test
	fun when_measureUnconstrainedViewSize_then_reportsArbitraryDimensions() = runTuIndiceUiTest {
		setTuIndiceTestContent(density = 1f) {
			MeasureUnconstrainedViewSize(
				viewToMeasure = {
					Box(modifier = Modifier.size(width = 12.dp, height = 34.dp))
				}
			) { measuredSize ->
				Text("${measuredSize.width.value.toInt()}x${measuredSize.height.value.toInt()}")
			}
		}

		onNodeWithText("12x34").assertIsDisplayed()
	}
}
