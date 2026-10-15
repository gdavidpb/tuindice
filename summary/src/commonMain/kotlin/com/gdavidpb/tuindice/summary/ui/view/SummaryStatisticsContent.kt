package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

// The six blocks of the Statistics screen, in the order the screen lists them, in one column that scrolls under the top
// bar. The chart action label is the one every chart of the screen offers to a screen reader.
@Composable
fun SummaryStatisticsContent(
	statistics: SummaryStatisticsItem,
	actions: SummaryHubActions
) {
	val chartActionLabel = statistics.chartActionLabel.asString()

	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsContent)
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(
				start = TuIndiceSpacing.Screen,
				top = InternalScreenDefaults.TopBarSpacing + TuIndiceSpacing.Medium,
				end = TuIndiceSpacing.Screen,
				bottom = TuIndiceSpacing.Wide
			),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Screen)
	) {
		SummaryStatisticsCurrentTermView(
			item = statistics.currentTerm,
			chartActionLabel = chartActionLabel,
			actions = actions
		)

		SummaryStatisticsIndexView(
			item = statistics.index,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsSubjectsView(
			item = statistics.subjects,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsCreditsView(
			item = statistics.credits,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsGradesView(
			item = statistics.grades,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsPaceView(
			item = statistics.pace,
			chartActionLabel = chartActionLabel
		)
	}
}
