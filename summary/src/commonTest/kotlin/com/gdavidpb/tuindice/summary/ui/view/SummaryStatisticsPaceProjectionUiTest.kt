package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsPaceProjectionUiTest {
	@Test
	fun when_thePensumIsRemaining_then_ItsCountIsWrittenWithItsTag() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProjectionHost(
				projection = SummaryStatisticsPaceItem.Projection.Remaining(
					fact = SummaryStatisticsFactItem(value = "6", label = words("trimestres")),
					tag = words("Estimado")
				)
			)
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsProjection)
		onNodeWithText("Estimado").assertIsDisplayed()
	}

	@Test
	fun when_noPensumIsCached_then_theProjectionInvitesToOpenIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ProjectionHost(
				projection = SummaryStatisticsPaceItem.Projection.NoPensum(
					title = words("Proyección pendiente"),
					message = words("Abre tu pensum para proyectar el ritmo.")
				)
			)
		}
		waitForIdle()

		onNodeWithText("Abre tu pensum para proyectar el ritmo.").assertIsDisplayed()
	}

	@Composable
	private fun ProjectionHost(projection: SummaryStatisticsPaceItem.Projection) {
		Column {
			SummaryStatisticsPaceProjection(projection = projection)
		}
	}
}
