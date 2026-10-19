package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportLegendItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags

// The count of each outcome, each in its colour, in the order the screen lists them.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TermReportLegendView(
	legend: List<TermReportLegendItem>,
	modifier: Modifier = Modifier
) {
	FlowRow(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportLegend),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		legend.forEach { item ->
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
			) {
				Box(
					modifier = Modifier
						.size(LegendDotSize)
						.background(color = item.tone.color(), shape = CircleShape)
				)

				Text(
					text = item.text.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

private val LegendDotSize = 8.dp
