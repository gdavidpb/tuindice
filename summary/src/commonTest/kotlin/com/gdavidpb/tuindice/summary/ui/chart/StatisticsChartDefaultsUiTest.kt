package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsChartDefaultsUiTest {
	@Test
	fun when_lightTheme_then_dataIsTheLightLoadBand() = runTuIndiceUiTest {
		var dataColor = Color.Unspecified

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				dataColor = StatisticsChartDefaults.dataColor()
			}
		}
		waitForIdle()

		assertEquals(AcademicStatusColors.LoadBandLightLight, dataColor)
	}

	@Test
	fun when_darkTheme_then_dataIsTheDarkLoadBand() = runTuIndiceUiTest {
		var dataColor = Color.Unspecified

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				dataColor = StatisticsChartDefaults.dataColor()
			}
		}
		waitForIdle()

		assertEquals(AcademicStatusColors.LoadBandLightDark, dataColor)
	}
}
