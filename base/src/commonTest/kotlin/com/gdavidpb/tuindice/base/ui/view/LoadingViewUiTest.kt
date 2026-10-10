package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LoadingViewUiTest {
	@Test
	fun when_indicatorTagProvided_then_tagsAnIndeterminateProgressIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadingView(indicatorTag = IndicatorTag)
		}

		assertNodeVisible(IndicatorTag)
		onNodeWithTag(IndicatorTag).assert(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
	}

	@Test
	fun when_indicatorTagIsOmitted_then_stillShowsTheIndicatorUntagged() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadingView()
		}

		assertNodeHidden(IndicatorTag)
		assertEquals(
			expected = 1,
			actual = onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
				.fetchSemanticsNodes().size
		)
		onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
	}

	@Test
	fun when_rendered_then_fillsTheAvailableSpaceAndCentersTheIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Compact) {
			LoadingView(
				modifier = Modifier.testTag(ContainerTag),
				indicatorTag = IndicatorTag
			)
		}

		onNodeWithTag(ContainerTag)
			.assertWidthIsEqualTo(TuIndiceTestSizeClass.Compact.widthDp.dp)
			.assertHeightIsEqualTo(TuIndiceTestSizeClass.Compact.heightDp.dp)

		val containerBounds = onNodeWithTag(ContainerTag).fetchSemanticsNode().boundsInRoot
		val indicatorBounds = onNodeWithTag(IndicatorTag).fetchSemanticsNode().boundsInRoot

		assertTrue(abs(containerBounds.center.x - indicatorBounds.center.x) <= CENTER_TOLERANCE_PX)
		assertTrue(abs(containerBounds.center.y - indicatorBounds.center.y) <= CENTER_TOLERANCE_PX)
	}

	private companion object {
		const val ContainerTag = "loading_view_container"
		const val IndicatorTag = "loading_view_indicator"
		const val CENTER_TOLERANCE_PX = 1f
	}
}
