package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryLeadTextViewUiTest {
	@Test
	fun when_leadIsShown_then_itSaysTheDay() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLeadTextView(text = "Vie 16")
		}

		onNodeWithText("Vie 16").assertIsDisplayed()
	}

	@Test
	fun when_leadIsShort_then_itKeepsTheWidthOfTheLeadColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLeadTextView(modifier = Modifier.testTag("lead"), text = "Hoy")
		}

		onNodeWithTag("lead").assertWidthIsAtLeast(64.dp)
	}

	@Test
	fun when_leadIsADayWithItsNumberAtTheDefaultLetter_then_itIsExactlyTheWidthOfTheLeadColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryLeadTextView(modifier = Modifier.testTag("lead"), text = "Vie 16")
		}

		onNodeWithTag("lead").assertWidthIsEqualTo(64.dp)
	}

	@Test
	fun when_leadIsWiderThanTheLeadColumn_then_itKeepsTheSamePaddingOnBothSidesThePillHas() = runTuIndiceUiTest {
		lateinit var density: Density

		setTuIndiceTestContent {
			density = LocalDensity.current

			Row {
				SummaryLeadTextView(modifier = Modifier.testTag("lead"), text = "Miércoles 28")
				Text(
					modifier = Modifier.testTag("plain"),
					text = "Miércoles 28",
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Medium
				)
			}
		}

		// Wider than the column, the lead is its text plus the padding of the pill (4dp a side).
		val lead = onNodeWithTag("lead").fetchSemanticsNode().size.width
		val plain = onNodeWithTag("plain").fetchSemanticsNode().size.width
		val padding = with(density) { (TuIndiceSpacing.XSmall * 2).roundToPx() }

		assertTrue(abs(lead - plain - padding) <= 1, "the lead is ${lead - plain} px wider than its text; $padding expected")
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theDayIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryLeadTextView(text = "Mié 28")
			}
		}

		assertTextIsWhole("Mié 28", singleLine = true)
	}
}
