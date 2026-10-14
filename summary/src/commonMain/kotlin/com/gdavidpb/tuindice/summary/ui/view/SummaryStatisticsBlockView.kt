package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * A block of the Statistics screen: its section header, with the link that leads from it when it has one, and its
 * [content] in the card the hub uses, so the screen reads as the hub does.
 */
@Composable
fun SummaryStatisticsBlockView(
	head: SummaryStatisticsBlockHead,
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(head.tag),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = head.title,
			linkText = head.linkText,
			onLinkClick = head.onLinkClick,
			sectionTag = head.tag
		)

		SummaryHubCardView {
			Column(
				modifier = Modifier.padding(TuIndiceSpacing.Screen),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
				content = content
			)
		}
	}
}
