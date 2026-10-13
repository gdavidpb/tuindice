package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryHubCardViewUiTest {
	@Test
	fun when_cardHasRows_then_theyAreInsideItAndItFillsTheWidth() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubCardView(modifier = Modifier.testTag("card")) {
				Text("first row")
				Text("second row")
			}
		}

		onNodeWithText("first row").assertIsDisplayed()
		onNodeWithText("second row").assertIsDisplayed()
		onNodeWithTag("card").assertWidthIsEqualTo(360.dp)
	}

	@Test
	fun when_cardHasABorder_then_itStillHoldsItsContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				SummaryHubCardView(modifier = Modifier.testTag("plain")) { Text("plain content") }
				SummaryHubCardView(modifier = Modifier.testTag("bordered"), hasBorder = true) {
					Text("bordered content")
				}
			}
		}

		onNodeWithText("plain content").assertIsDisplayed()
		onNodeWithText("bordered content").assertIsDisplayed()
		onNodeWithTag("bordered").assertWidthIsEqualTo(360.dp)
	}
}
