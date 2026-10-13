package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryHubDividerViewUiTest {
	@Test
	fun when_dividerIsShown_then_itIsAHairlineAsWideAsItsParent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubDividerView(modifier = Modifier.testTag("divider"))
		}

		onNodeWithTag("divider").assertIsDisplayed()
		onNodeWithTag("divider").assertHeightIsEqualTo(1.dp)
		onNodeWithTag("divider").assertWidthIsEqualTo(360.dp)
	}

	@Test
	fun when_dividerIsShown_then_itIsAtTheTopOfItsBox() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubDividerView(modifier = Modifier.testTag("divider"))
		}

		assertEquals(0.dp, onNodeWithTag("divider").getUnclippedBoundsInRoot().top)
	}
}
