package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class RecordViewModeBannerColorsProviderUiTest {
	@Test
	fun when_theModeIsHistorical_then_theBannerTakesThePrimaryContainerPair() = runTuIndiceUiTest {
		var colors: RecordViewModeBannerColors? = null
		var scheme: ColorScheme? = null

		setTuIndiceTestContent {
			scheme = MaterialTheme.colorScheme
			colors = recordViewModeBannerColors(mode = RecordViewMode.Historical)
		}

		waitForIdle()

		val theme = assertNotNull(scheme)

		assertEquals(
			expected = RecordViewModeBannerColors(
				containerColor = theme.primaryContainer,
				contentColor = theme.onPrimaryContainer
			),
			actual = colors
		)
	}

	@Test
	fun when_theModeIsProjection_then_theBannerTakesTheTertiaryPair_soTheModesLookApart() = runTuIndiceUiTest {
		var historical: RecordViewModeBannerColors? = null
		var projection: RecordViewModeBannerColors? = null
		var scheme: ColorScheme? = null

		setTuIndiceTestContent {
			scheme = MaterialTheme.colorScheme
			historical = recordViewModeBannerColors(mode = RecordViewMode.Historical)
			projection = recordViewModeBannerColors(mode = RecordViewMode.Projection)
		}

		waitForIdle()

		val theme = assertNotNull(scheme)

		assertEquals(
			expected = RecordViewModeBannerColors(
				containerColor = theme.tertiaryContainer,
				contentColor = theme.onTertiaryContainer
			),
			actual = projection
		)
		// The banner is how a glance tells a simulation from the closed record.
		assertNotEquals(historical, projection)
	}

	@Test
	fun when_theThemeIsDark_then_theColorsFollowTheThemeInForce_notAFixedPalette() = runTuIndiceUiTest {
		val dark = darkColorScheme()
		var projection: RecordViewModeBannerColors? = null

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = dark) {
				projection = recordViewModeBannerColors(mode = RecordViewMode.Projection)
			}
		}

		waitForIdle()

		assertEquals(
			expected = RecordViewModeBannerColors(
				containerColor = dark.tertiaryContainer,
				contentColor = dark.onTertiaryContainer
			),
			actual = projection
		)
	}
}
