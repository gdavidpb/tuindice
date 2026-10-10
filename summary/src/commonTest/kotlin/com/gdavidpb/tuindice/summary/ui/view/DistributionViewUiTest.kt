package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DistributionViewUiTest {
	@Test
	fun when_twoSharesSplitATrack_then_eachTakesTheWidthOfItsWeight_underItsFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row(modifier = Modifier.width(200.dp)) {
				DistributionView(label = "3", weight = 3f, color = Color.Green)
				DistributionView(label = "1", weight = 1f, color = Color.Red)
			}
		}

		// Three parts of four, then the one left: the bar of each share is as wide as its figure's node.
		onNodeWithText("3").assertIsDisplayed().assertWidthIsEqualTo(150.dp)
		onNodeWithText("1").assertIsDisplayed().assertWidthIsEqualTo(50.dp)
		onNodeWithText("1").assertLeftPositionInRootIsEqualTo(
			onNodeWithText("3").getUnclippedBoundsInRoot().right
		)
	}

	@Test
	fun when_aModifierIsGiven_then_itReachesTheShare_whichKeepsItsWeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row(modifier = Modifier.width(200.dp)) {
				// The empty track of a card with nothing counted yet: tagged, with a blank figure.
				DistributionView(
					modifier = Modifier.testTag(TrackTag),
					label = "",
					weight = 1f,
					color = Color.Gray
				)
			}
		}

		assertNodeVisible(TrackTag)
		onNodeWithTag(TrackTag)
			.assertTextEquals("")
			.assertWidthIsEqualTo(200.dp)
	}

	private companion object {
		const val TrackTag = "distribution_track"
	}
}
