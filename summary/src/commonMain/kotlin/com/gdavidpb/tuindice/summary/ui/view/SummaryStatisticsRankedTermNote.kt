package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults

// "◯ Mejor: Ene - Mar 2024 · 4.5714" and "◯ Peor: …": the ring before the words is the one around that point
// on the chart, in the same colour.
@Composable
fun SummaryStatisticsRankedTermNote(
	text: String,
	color: Color,
	tag: String
) {
	Row(
		modifier = Modifier.testTag(tag),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Box(
			modifier = Modifier
				.size(NoteRingSize)
				.border(width = StatisticsChartDefaults.RingStrokeWidth, color = color, shape = CircleShape)
		)

		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

private val NoteRingSize = 10.dp
