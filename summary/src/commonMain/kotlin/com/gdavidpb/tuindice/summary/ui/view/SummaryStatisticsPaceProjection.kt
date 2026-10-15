package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsEmptyHeading

// What the pensum still takes at the pace of the closed terms: the count of its regular terms, or the words that say
// why it cannot be counted yet, or that it is complete. Without a cached pensum it invites to open one.
@Composable
fun ColumnScope.SummaryStatisticsPaceProjection(projection: SummaryStatisticsPaceItem.Projection) {
	when (projection) {
		is SummaryStatisticsPaceItem.Projection.Remaining -> SummaryStatisticsRemainingProjection(projection = projection)

		is SummaryStatisticsPaceItem.Projection.Pending -> Text(
			modifier = Modifier.testTag(SummaryUiTags.StatisticsProjection),
			text = projection.text.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		is SummaryStatisticsPaceItem.Projection.Complete -> Text(
			modifier = Modifier.testTag(SummaryUiTags.StatisticsProjection),
			text = projection.text.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		is SummaryStatisticsPaceItem.Projection.NoPensum -> SummaryStatisticsEmptyView(
			heading = SummaryStatisticsEmptyHeading(
				title = projection.title.asString(),
				icon = Icons.Outlined.AccountTree
			),
			message = projection.message.asString()
		)

		SummaryStatisticsPaceItem.Projection.None -> Unit
	}
}
