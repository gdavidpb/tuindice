package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// The label written under each column, centred on it, in the band under the plot. The band is part of the chart's
// content, so the labels scroll with their columns. A point already says its own name to a screen reader, so the label
// is hidden from it rather than read a second time.
@Composable
internal fun BoxScope.StatisticsAxisLabels(
	key: String,
	labels: List<String>,
	columnWidth: Float
) {
	labels.forEachIndexed { index, text ->
		Text(
			modifier = Modifier
				.offset(x = (index * columnWidth).dp, y = StatisticsChartDefaults.AxisLabelTop)
				.width(columnWidth.dp)
				.height(StatisticsChartDefaults.AxisLabelBandHeight)
				.testTag(StatisticsLabelTags.axis(key, index))
				.semantics { hideFromAccessibility() },
			text = text,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
