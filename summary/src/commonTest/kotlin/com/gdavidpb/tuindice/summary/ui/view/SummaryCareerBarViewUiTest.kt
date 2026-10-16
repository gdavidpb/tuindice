package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryCareerBarViewUiTest {
	@Test
	fun when_itHasADescription_then_itIsAProgressElement() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerBarView(fraction = 0.65f, description = "Avance del pensum: 65 %")
		}

		onAllNodesWithTag(SummaryUiTags.CareerProgressBar).assertCountEquals(1)
	}

	@Test
	fun when_itHasNoDescription_then_itIsOnlyDecoration() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerBarView(fraction = 0f, description = null)
		}

		onAllNodesWithTag(SummaryUiTags.CareerProgressBar).assertCountEquals(0)
	}
}
