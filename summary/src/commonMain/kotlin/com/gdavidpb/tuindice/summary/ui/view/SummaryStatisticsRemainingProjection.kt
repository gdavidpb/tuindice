package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The projection's count as a compact figure, with its tag in an outline beside it.
@Composable
fun SummaryStatisticsRemainingProjection(projection: SummaryStatisticsPaceItem.Projection.Remaining) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsProjection),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		SummaryStatisticsFactsView(
			facts = listOf(projection.fact),
			modifier = Modifier.weight(1f)
		)

		Text(
			modifier = Modifier
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.outline,
					shape = RoundedCornerShape(TuIndiceRadius.Small)
				)
				.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.Two)
				.testTag(SummaryUiTags.StatisticsProjectionTag),
			text = projection.tag.asString(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
