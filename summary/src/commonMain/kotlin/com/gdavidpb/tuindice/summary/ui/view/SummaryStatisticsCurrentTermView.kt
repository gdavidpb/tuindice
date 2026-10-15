package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsEmptyHeading

/**
 * "Este trimestre": the term in progress as its evaluations tell it. Its link, the name of the term, leads to Record.
 * The block carries the points evaluated out of the total, one row for each evaluation type with its bar and its score,
 * and the evaluations that went by without a grade, which lead to the week of the oldest. A row with a grade opens its
 * tooltip when it is tapped. With no term in progress, or with its evaluations not downloaded, it says so instead.
 */
@Composable
fun SummaryStatisticsCurrentTermView(
	item: SummaryStatisticsCurrentTermItem,
	chartActionLabel: String,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	val termTarget = item.termTarget
	val head = SummaryStatisticsBlockHead(
		tag = SummaryUiTags.StatisticsCurrentTermBlock,
		title = item.title.asString(),
		linkText = item.termLink?.asString()?.takeIf { termTarget != null },
		onLinkClick = { termTarget?.let(actions.onTargetClick) }
	)

	SummaryStatisticsBlockView(head = head, modifier = modifier) {
		when (val content = item.content) {
			is SummaryStatisticsCurrentTermItem.Content.NoTerm -> SummaryStatisticsEmptyView(
				heading = SummaryStatisticsEmptyHeading(
					title = content.title.asString(),
					icon = Icons.Outlined.CalendarMonth
				),
				message = content.message.asString()
			)

			is SummaryStatisticsCurrentTermItem.Content.NotSynced -> SummaryStatisticsEmptyView(
				heading = SummaryStatisticsEmptyHeading(
					title = content.title.asString(),
					icon = Icons.AutoMirrored.Outlined.Assignment
				),
				message = content.message.asString(),
				actionText = content.action.asString(),
				onActionClick = { actions.onTargetClick(content.target) }
			)

			is SummaryStatisticsCurrentTermItem.Content.NoEvaluations -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsCurrentTermItem.Content.Data -> SummaryStatisticsCurrentTermData(
				data = content,
				chartActionLabel = chartActionLabel,
				actions = actions
			)
		}
	}
}
