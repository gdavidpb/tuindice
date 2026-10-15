package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsTarget
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsTooltipTarget
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

			is SummaryStatisticsCurrentTermItem.Content.Data -> TermData(
				data = content,
				chartActionLabel = chartActionLabel,
				actions = actions
			)
		}
	}
}

// The figures of the term in progress: its points out of the total with their track, the rows of its evaluation types,
// and the line of the evaluations that went by without a grade. One row's tooltip is open at a time.
@Composable
private fun ColumnScope.TermData(
	data: SummaryStatisticsCurrentTermItem.Content.Data,
	chartActionLabel: String,
	actions: SummaryHubActions
) {
	val openRow = remember { mutableStateOf<Int?>(null) }

	data.figure?.let { figure -> SummaryStatisticsFigureView(figure = figure) }

	data.fraction?.let { fraction ->
		// The data blue of the statistics, as every chart of the screen draws its figures.
		TuIndiceProgressBar(
			fraction = fraction,
			color = StatisticsChartDefaults.dataColor(),
			fillTag = SummaryUiTags.StatisticsTrackFill
		)
	}

	data.rows.forEachIndexed { index, row ->
		TypeRow(
			index = index,
			row = row,
			openRow = openRow,
			chartActionLabel = chartActionLabel
		)
	}

	data.overdue?.let { overdue ->
		OverdueRow(overdue = overdue, actions = actions)
	}
}

// One evaluation type: its bar and its score. A type with a grade opens a tooltip that names its points and its share.
@Composable
private fun TypeRow(
	index: Int,
	row: SummaryStatisticsEvaluationTypeItem,
	openRow: MutableState<Int?>,
	chartActionLabel: String
) {
	val tooltip = row.tooltip

	if (tooltip == null) {
		TypeRowContent(row = row)
	} else {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = StatisticsChartDefaults.TargetMinSize)
		) {
			StatisticsTooltipTarget(
				target = StatisticsTarget(
					tag = SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.CurrentTerm, index),
					description = tooltip.title.asString(),
					actionLabel = chartActionLabel,
					tooltip = tooltip.toRichText()
				),
				isOpen = openRow.value == index,
				onOpenChange = { open -> openRow.value = if (open) index else null }
			) {
				TypeRowContent(row = row)
			}
		}
	}
}

// The name of the type, its track, and the words that say how many of its evaluations are graded and what they score.
@Composable
private fun TypeRowContent(row: SummaryStatisticsEvaluationTypeItem) {
	val onSurface = MaterialTheme.colorScheme.onSurface

	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Text(
			text = row.name.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = onSurface
		)

		TuIndiceProgressBar(
			fraction = row.fraction,
			color = StatisticsChartDefaults.dataColor(),
			fillTag = SummaryUiTags.StatisticsTrackFill
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = row.gradedText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Text(
				text = row.scoreText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = onSurface
			)
		}
	}
}

// The evaluations that went by without a grade, as a line with its warning and a rule above it. The line leads to the
// week of the oldest one when the hub has it, and then it shows an arrow.
@Composable
private fun OverdueRow(
	overdue: SummaryStatisticsCurrentTermItem.Overdue,
	actions: SummaryHubActions
) {
	val target = overdue.target

	HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = TuIndiceComponentSizes.IconButton)
			.testTag(SummaryUiTags.StatisticsOverdue)
			.then(
				if (target == null) {
					Modifier
				} else {
					Modifier.clickable(role = Role.Button, onClick = { actions.onTargetClick(target) })
				}
			),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.Icon),
			imageVector = Icons.Outlined.WarningAmber,
			contentDescription = null,
			tint = AcademicStatusColors.warning()
		)

		Text(
			modifier = Modifier.weight(1f),
			text = overdue.text.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
		)

		if (target != null) {
			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
			)
		}
	}
}
