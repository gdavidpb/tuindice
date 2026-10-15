package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartGeometry
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_statistics
import tuindice.summary.generated.resources.summary_link_statistics
import tuindice.summary.generated.resources.summary_section_statistics
import tuindice.summary.generated.resources.summary_statistics_index_suffix
import tuindice.summary.generated.resources.summary_statistics_section_empty_message
import tuindice.summary.generated.resources.summary_statistics_section_empty_title
import tuindice.summary.generated.resources.summary_statistics_section_first_message
import tuindice.summary.generated.resources.summary_statistics_section_first_title

/**
 * "Estadísticas": the section of the hub under the career. With terms over it is one card that leads to the Statistics
 * screen: the cumulative average, its line, which is decoration, and the phrase of the subjects approved with the best
 * term. Without a term over, a term in progress invites to read the statistics once it is over and leads there. With
 * neither, the card says what the statistics will show, with no link.
 */
@Composable
fun SummaryStatisticsSectionView(
	item: SummaryStatisticsSectionItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	val linkText = if (item == SummaryStatisticsSectionItem.Empty) {
		null
	} else {
		stringResource(Res.string.summary_link_statistics)
	}

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsSection),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = stringResource(Res.string.summary_section_statistics),
			linkText = linkText,
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Statistics) },
			sectionTag = SummaryUiTags.StatisticsSection
		)

		when (item) {
			SummaryStatisticsSectionItem.Empty -> SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Statistics,
					titleText = UiText.Resource(Res.string.summary_statistics_section_empty_title),
					messageText = UiText.Resource(Res.string.summary_statistics_section_empty_message)
				),
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.StatisticsEmptyCard)
			)

			SummaryStatisticsSectionItem.Invite -> SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Statistics,
					titleText = UiText.Resource(Res.string.summary_statistics_section_first_title),
					messageText = UiText.Resource(Res.string.summary_statistics_section_first_message),
					target = SummaryHubTarget.Statistics,
					targetLabel = UiText.Resource(Res.string.a11y_summary_open_statistics)
				),
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.StatisticsInviteCard)
			)

			is SummaryStatisticsSectionItem.Figures -> FiguresCard(
				item = item,
				onClick = { actions.onTargetClick(SummaryHubTarget.Statistics) }
			)
		}
	}
}

// The card of the terms over. The whole card is one button that leads to the Statistics screen, so its line and its
// phrase are part of that button and take no touch of their own.
@Composable
private fun FiguresCard(
	item: SummaryStatisticsSectionItem.Figures,
	onClick: () -> Unit
) {
	SummaryHubCardView(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsCard)
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_statistics),
				role = Role.Button,
				onClick = onClick
			)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			item.grade?.let { grade ->
				SummaryGradeFigureView(
					tag = SummaryUiTags.StatisticsFigureText,
					grade = grade,
					label = stringResource(Res.string.summary_statistics_index_suffix),
						style = MaterialTheme.typography.titleLarge
				)
			}

			if (item.line.isNotEmpty()) {
				SummaryStatisticsSparkline(values = item.line)
			}

			Text(
				modifier = Modifier.testTag(SummaryUiTags.StatisticsPhrase),
				text = item.phrase.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}
	}
}

// The cumulative averages in order, as a thin line scaled from the lowest of them to the highest, in the colour of the
// data. It is decoration: a screen reader does not read it. A single average is drawn as a dot.
@Composable
private fun SummaryStatisticsSparkline(values: List<Float>) {
	val color = StatisticsChartDefaults.dataColor()
	val minValue = values.minOrNull() ?: 0f
	val maxValue = values.maxOrNull() ?: 0f

	Canvas(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsSparkline)
			.fillMaxWidth()
			.height(SparklineHeight)
			.clearAndSetSemantics { }
	) {
		// The line keeps half its width clear of the edges, so its ends are not cut.
		val strokePx = StatisticsChartDefaults.LineStrokeWidth.toPx()
		val bandHeight = size.height - strokePx * 2

		val points = values.mapIndexed { index, value ->
			val x = if (values.size == 1) size.width / 2 else size.width * index / (values.size - 1)
			val fraction = StatisticsChartGeometry.valueFraction(value = value, minValue = minValue, maxValue = maxValue)

			Offset(x = x, y = strokePx + bandHeight * (1f - fraction))
		}

		if (points.size == 1) {
			drawCircle(
				color = color,
				radius = StatisticsChartDefaults.MarkerSize.toPx() / 2,
				center = points.single()
			)
		} else {
			val path = Path()

			points.forEachIndexed { index, point ->
				if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
			}

			drawPath(
				path = path,
				color = color,
				style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
			)
		}
	}
}

private val SparklineHeight = 44.dp
