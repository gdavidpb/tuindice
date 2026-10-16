package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_statistics
import tuindice.summary.generated.resources.summary_link_statistics
import tuindice.summary.generated.resources.summary_section_statistics
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
	val linkText = if (item.hasLink) stringResource(Res.string.summary_link_statistics) else null

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
					icon = Icons.Outlined.BarChart,
					titleText = UiText.Resource(Res.string.summary_statistics_section_empty_title),
					messageText = UiText.Resource(Res.string.summary_statistics_section_empty_message)
				),
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.StatisticsEmptyCard)
			)

			SummaryStatisticsSectionItem.Invite -> SummaryCardView(
				item = SummaryCardItem(
					icon = Icons.Outlined.BarChart,
					titleText = UiText.Resource(Res.string.summary_statistics_section_first_title),
					messageText = UiText.Resource(Res.string.summary_statistics_section_first_message),
					target = SummaryHubTarget.Statistics,
					targetLabel = UiText.Resource(Res.string.a11y_summary_open_statistics)
				),
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.StatisticsInviteCard)
			)

			is SummaryStatisticsSectionItem.Figures -> SummaryStatisticsFiguresCard(
				item = item,
				onClick = { actions.onTargetClick(SummaryHubTarget.Statistics) }
			)
		}
	}
}
