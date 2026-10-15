package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The credits the pensum has approved: their figure with the share at its end, and the bar that follows them in
// the approved colour. The bar is a progress element for a screen reader, which hears the figure and its share as
// one sentence.
@Composable
fun ColumnScope.SummaryStatisticsCreditsData(
	data: SummaryStatisticsCreditsItem.Content.Data
) {
	val fraction = data.fraction
	val description = data.description.asString()

	SummaryStatisticsFigureView(figure = data.figure)

	TuIndiceProgressBar(
		fraction = fraction,
		color = AcademicStatusColors.approved(),
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsPensumTrack)
			.clearAndSetSemantics {
				contentDescription = description
				progressBarRangeInfo = ProgressBarRangeInfo(current = fraction, range = 0f..1f)
			},
		fillTag = SummaryUiTags.StatisticsTrackFill
	)
}
