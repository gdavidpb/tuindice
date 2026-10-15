package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryGradeFigureViewUiTest {
	@Test
	fun when_gradeIsGiven_then_itIsWrittenBesideItsLabelOnceItHasCountedUp() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryGradeFigureView(
				grade = 4.125f,
				label = "acumulado",
				style = MaterialTheme.typography.headlineMedium,
				tag = SummaryUiTags.StatisticsFigure
			)
		}

		advanceAnimationsBy(1_000)

		onNodeWithTag(SummaryUiTags.StatisticsFigure).assertTextEquals("4.1250 / 5.0")
		onNodeWithText("acumulado").assertIsDisplayed()
	}

	@Test
	fun when_theAnimationsAreOff_then_theGradeIsWrittenAtOnce() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryGradeFigureView(
					grade = 4.25f,
					label = "acumulado",
					style = MaterialTheme.typography.titleLarge,
					tag = SummaryUiTags.StatisticsFigureText
				)
			}
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsFigureText).assertTextEquals("4.2500 / 5.0")
	}
}
