package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.assertTooltipShows
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsPaceViewUiTest {
	@Test
	fun when_theProjectionIsKnown_then_itsCountIsAFactAndItsTagIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsPaceView(item = paceWith(projection = remaining()), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsProjectionTag).assertTextEquals("Proyección")
		assertNodeVisible(SummaryUiTags.StatisticsProjection)
		onNodeWithText("4").assertIsDisplayed()
		onNodeWithText("trimestres que faltan").assertIsDisplayed()
	}

	@Test
	fun when_theInfoMarkIsTapped_then_itsTooltipExplainsTheLineAndTheProjection() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsPaceView(item = paceWith(projection = remaining()), chartActionLabel = "Ver detalle")
		}
		waitForIdle()

		onNodeWithTag(SummarySectionUiTags.info(SummaryUiTags.StatisticsPaceBlock)).performClick()

		assertTooltipShows(EXPLANATION)
	}

	@Test
	fun when_noPensumIsCached_then_theInvitationToOpenItIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsPaceView(
				item = paceWith(
					projection = SummaryStatisticsPaceItem.Projection.NoPensum(
						title = words("Calcula cuánto te falta"),
						message = words("Abre tu pensum para calcular tu ritmo.")
					)
				),
				chartActionLabel = "Ver detalle"
			)
		}

		onNodeWithText("Calcula cuánto te falta").assertIsDisplayed()
		onNodeWithText("Abre tu pensum para calcular tu ritmo.").assertIsDisplayed()
	}

	@Test
	fun when_theReferenceIsDrawn_then_itsLabelIsWrittenAtItsEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsPaceView(
				item = paceWith(projection = SummaryStatisticsPaceItem.Projection.None),
				chartActionLabel = "Ver detalle"
			)
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsReferenceLabel).assertTextEquals("promedio 13.8")
	}

	private fun remaining(): SummaryStatisticsPaceItem.Projection.Remaining {
		return SummaryStatisticsPaceItem.Projection.Remaining(
			fact = SummaryStatisticsFactItem(value = "4", label = words("trimestres que faltan")),
			tag = words("Proyección")
		)
	}

	private fun paceWith(projection: SummaryStatisticsPaceItem.Projection): SummaryStatisticsPaceItem {
		return SummaryStatisticsPaceItem(
			title = words("Ritmo"),
			content = SummaryStatisticsPaceItem.Content.Data(
				figure = figureOf(value = "13.8", label = "UC aprobadas por trimestre"),
				bars = listOf(
					SummaryStatisticsBarItem(
						axisLabel = words("S-D 23"),
						value = 16f,
						tooltip = tooltipOf("Sep - Dic 2023: 16 UC", "5 materias aprobadas")
					),
					SummaryStatisticsBarItem(
						axisLabel = words("E-M 24"),
						value = 12f,
						tooltip = tooltipOf("Ene - Mar 2024: 12 UC", "4 materias aprobadas")
					)
				),
				reference = SummaryStatisticsReferenceItem(value = 13.8f, label = words("promedio 13.8")),
				projection = projection
			),
			info = words(EXPLANATION)
		)
	}

	private companion object {
		const val EXPLANATION = "La línea es tu promedio de créditos por trimestre regular."
	}
}
