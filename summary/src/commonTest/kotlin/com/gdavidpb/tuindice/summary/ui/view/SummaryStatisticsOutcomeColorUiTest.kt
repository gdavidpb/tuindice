package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsOutcomeColorUiTest {
	@Test
	fun when_approved_then_itIsTheApprovalStatusColorOfTheLightTheme() = runTuIndiceUiTest {
		var color = Color.Unspecified

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides false) {
				color = SummaryStatisticsOutcome.APPROVED.color()
			}
		}
		waitForIdle()

		assertEquals(AcademicStatusColors.ApprovedLight, color)
	}

	@Test
	fun when_failed_then_itIsTheErrorColorOfTheTheme() = runTuIndiceUiTest {
		var color = Color.Unspecified
		var errorColor = Color.Unspecified

		setTuIndiceTestContent {
			color = SummaryStatisticsOutcome.FAILED.color()
			errorColor = MaterialTheme.colorScheme.error
		}
		waitForIdle()

		assertEquals(errorColor, color)
	}

	@Test
	fun when_withdrawn_then_itIsTheAvailableStatusColorOfTheDarkTheme() = runTuIndiceUiTest {
		var color = Color.Unspecified

		setTuIndiceTestContent {
			CompositionLocalProvider(TuIndiceDarkTheme.Local provides true) {
				color = SummaryStatisticsOutcome.WITHDRAWN.color()
			}
		}
		waitForIdle()

		assertEquals(AcademicStatusColors.AvailableDark, color)
	}
}
