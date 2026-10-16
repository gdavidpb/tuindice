package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleNowLineViewUiTest {
	@Test
	fun when_theLineIsDrawn_then_itIsReadAsAhora() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleNowLineView(modifier = Modifier.width(72.dp))
		}

		assertNodeVisible(RecordUiTags.ScheduleNowLine)
		onNodeWithTag(RecordUiTags.ScheduleNowLine).assertContentDescriptionEquals("Ahora")
	}

	@Test
	fun when_theLineIsGivenAColumn_then_itCrossesAllOfIt_atTheHeightOfItsDot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleNowLineView(modifier = Modifier.width(144.dp))
		}

		// The dot is the tallest part: the column offsets the line by half of it to centre it on the hour.
		onNodeWithTag(RecordUiTags.ScheduleNowLine)
			.assertWidthIsEqualTo(144.dp)
			.assertHeightIsEqualTo(ScheduleGridDefaults.NowDotSize)
	}

	@Test
	fun when_theThemeIsDark_then_theLineIsLightOverTheSheet_andDarkOverAMeeting() = runTuIndiceUiTest {
		setTuIndiceTestContent { NowLine(isDark = true) }

		// The meeting lies from 24dp to 48dp of the 72dp the line crosses.
		assertEquals(SheetInk, pixelOfTheLineAt(xFraction = BeforeTheMeeting), "before the meeting")
		assertEquals(CellInk, pixelOfTheLineAt(xFraction = OverTheMeeting), "over the meeting")
		assertEquals(SheetInk, pixelOfTheLineAt(xFraction = AfterTheMeeting), "after the meeting")
	}

	@Test
	fun when_theThemeIsLight_then_theLineIsTheSheetInkAllAlong() = runTuIndiceUiTest {
		setTuIndiceTestContent { NowLine(isDark = false) }

		assertEquals(SheetInk, pixelOfTheLineAt(xFraction = BeforeTheMeeting), "before the meeting")
		assertEquals(SheetInk, pixelOfTheLineAt(xFraction = OverTheMeeting), "over the meeting")
		assertEquals(SheetInk, pixelOfTheLineAt(xFraction = AfterTheMeeting), "after the meeting")
	}

	// The line over a meeting from 24dp to 48dp, on a scheme whose two inks cannot be mistaken for
	// each other or for the backdrop, at three pixels per dp so a sample falls well inside the stroke.
	@Composable
	private fun NowLine(isDark: Boolean) {
		val colorScheme = if (isDark) {
			darkColorScheme(onSurface = SheetInk, inverseOnSurface = CellInk)
		} else {
			lightColorScheme(onSurface = SheetInk, inverseOnSurface = CellInk)
		}

		CompositionLocalProvider(
			LocalDensity provides Density(PixelsPerDp),
			TuIndiceDarkTheme.Local provides isDark
		) {
			MaterialTheme(colorScheme = colorScheme) {
				Box(modifier = Modifier.background(Backdrop)) {
					ScheduleNowLineView(modifier = Modifier.width(72.dp), cellSpans = listOf(24.dp..48.dp))
				}
			}
		}
	}

	private fun ComposeUiTest.pixelOfTheLineAt(xFraction: Float): Color {
		waitForIdle()

		val pixels = onNodeWithTag(RecordUiTags.ScheduleNowLine).captureToImage().toPixelMap()

		return pixels[(pixels.width * xFraction).toInt(), pixels.height / 2]
	}

	private companion object {
		const val PixelsPerDp = 3f
		const val BeforeTheMeeting = 0.25f
		const val OverTheMeeting = 0.5f
		const val AfterTheMeeting = 0.85f

		val SheetInk = Color.Red
		val CellInk = Color.Blue
		val Backdrop = Color.White
	}
}
