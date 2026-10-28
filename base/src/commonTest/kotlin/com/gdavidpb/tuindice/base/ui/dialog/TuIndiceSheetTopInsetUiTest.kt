package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

// A sheet is laid out in the window below its top inset, the status bar's. These tests check the modifier that lays
// it out: a child taller than that space is measured at it, and a child that fits keeps its height.
@OptIn(ExperimentalTestApi::class)
class TuIndiceSheetTopInsetUiTest {
	@Test
	fun when_aChildIsTallerThanTheSpaceBelowTheInset_then_itIsMeasuredAtThatSpace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.fillMaxSize().testTag(WINDOW_TAG)) {
				Box(
					modifier = Modifier
						.belowTopInset(TOP_INSET_PX)
						.fillMaxWidth()
						.height(OVERSIZED_CHILD_DP.dp)
						.testTag(CHILD_TAG)
				)
			}
		}

		val windowHeightPx = onNodeWithTag(WINDOW_TAG).fetchSemanticsNode().size.height
		val childHeightPx = onNodeWithTag(CHILD_TAG).fetchSemanticsNode().size.height

		assertEquals(windowHeightPx - TOP_INSET_PX, childHeightPx)
	}

	@Test
	fun when_aChildFitsBelowTheInset_then_itKeepsItsHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column(modifier = Modifier.fillMaxSize()) {
				Box(modifier = Modifier.fillMaxWidth().height(FITTING_CHILD_DP.dp).testTag(PLAIN_TAG))
				Box(
					modifier = Modifier
						.belowTopInset(TOP_INSET_PX)
						.fillMaxWidth()
						.height(FITTING_CHILD_DP.dp)
						.testTag(CAPPED_TAG)
				)
			}
		}

		val plainHeightPx = onNodeWithTag(PLAIN_TAG).fetchSemanticsNode().size.height
		val cappedHeightPx = onNodeWithTag(CAPPED_TAG).fetchSemanticsNode().size.height

		assertEquals(plainHeightPx, cappedHeightPx)
	}

	private companion object {
		const val WINDOW_TAG = "tuindice_sheet_top_inset_test_window"
		const val CHILD_TAG = "tuindice_sheet_top_inset_test_child"
		const val PLAIN_TAG = "tuindice_sheet_top_inset_test_plain"
		const val CAPPED_TAG = "tuindice_sheet_top_inset_test_capped"
		const val TOP_INSET_PX = 60
		const val OVERSIZED_CHILD_DP = 3_000
		const val FITTING_CHILD_DP = 100
	}
}
