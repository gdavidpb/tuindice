package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The compact figures of a block, in one row of equal cells. Each reads its value at the title size, with its label
 * under it in the small label style, as the figures of the share card do. Nothing is written when there are none.
 */
@Composable
fun SummaryStatisticsFactsView(
	facts: List<SummaryStatisticsFactItem>,
	modifier: Modifier = Modifier
) {
	if (facts.isEmpty()) return

	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsFacts),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		facts.forEach { fact ->
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = fact.value,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface
				)

				Text(
					text = fact.label.asString(),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
