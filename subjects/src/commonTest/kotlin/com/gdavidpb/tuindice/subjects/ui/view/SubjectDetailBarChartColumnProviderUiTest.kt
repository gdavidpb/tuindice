package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.subjects.ui.model.SubjectChartDefaults
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class SubjectDetailBarChartColumnProviderUiTest {
	@Test
	fun when_providerRemembered_then_columnUsesDefaultBarStyle() = runTuIndiceUiTest {
		val providers = mutableListOf<ColumnCartesianLayer.ColumnProvider>()

		setTuIndiceTestContent {
			providers += rememberSubjectDetailBarChartColumnProvider()
		}

		waitForIdle()

		val column = providers.last().getWidestSeriesColumn(
			seriesKey = FIRST_SERIES_KEY,
			seriesIndex = 0,
			extraStore = MutableExtraStore()
		)

		assertEquals(Fill(SubjectChartDefaults.BarColor), column.fill)
		assertEquals(24.dp, column.thickness)
		assertEquals(RoundedCornerShape(TuIndiceRadius.Medium), column.shape)
	}

	@Test
	fun when_providerRemembered_then_everySeriesSharesTheSameColumn() = runTuIndiceUiTest {
		val providers = mutableListOf<ColumnCartesianLayer.ColumnProvider>()

		setTuIndiceTestContent {
			providers += rememberSubjectDetailBarChartColumnProvider()
		}

		waitForIdle()

		val extraStore = MutableExtraStore()
		val firstSeriesColumn = providers.last().getWidestSeriesColumn(
			seriesKey = FIRST_SERIES_KEY,
			seriesIndex = 0,
			extraStore = extraStore
		)
		val thirdSeriesColumn = providers.last().getWidestSeriesColumn(
			seriesKey = THIRD_SERIES_KEY,
			seriesIndex = 2,
			extraStore = extraStore
		)

		assertSame(firstSeriesColumn, thirdSeriesColumn)
	}

	@Test
	fun when_recomposed_then_returnsTheSameProviderInstance() = runTuIndiceUiTest {
		val providers = mutableListOf<ColumnCartesianLayer.ColumnProvider>()
		val recompositions = mutableIntStateOf(0)

		setTuIndiceTestContent {
			Text(text = "recomposicion ${recompositions.intValue}")
			providers += rememberSubjectDetailBarChartColumnProvider()
		}

		onNodeWithText("recomposicion 0").assertIsDisplayed()
		recompositions.intValue = 1
		onNodeWithText("recomposicion 1").assertIsDisplayed()

		assertEquals(2, providers.size)
		assertSame(providers.first(), providers.last())
	}

	private companion object {
		const val FIRST_SERIES_KEY = "serie_1"
		const val THIRD_SERIES_KEY = "serie_3"
	}
}
