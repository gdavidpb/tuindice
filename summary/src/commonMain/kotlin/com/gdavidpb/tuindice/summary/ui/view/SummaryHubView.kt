package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

/**
 * What Summary says under the profile, in this order: what needs attention, the report of a term not read yet, the
 * week, the term, the career and the statistics. Each is absent when it has nothing to say, and every line leads to the
 * screen that owns what it says.
 */
@Composable
fun SummaryHubView(
	hub: SummaryHubItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.ItemsList)
			.padding(horizontal = TuIndiceSpacing.Screen)
			.padding(bottom = TuIndiceSpacing.Screen),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Screen)
	) {
		if (hub.attention.isNotEmpty()) {
			SummaryAttentionView(
				items = hub.attention,
				onItemClick = actions.onTargetClick
			)
		}

		hub.termReport?.let { termReport ->
			SummaryTermReportView(
				item = termReport,
				onItemClick = actions.onTargetClick
			)
		}

		hub.week?.let { week ->
			SummaryWeekView(week = week, actions = actions)
		}

		hub.term?.let { term ->
			SummaryTermView(term = term, actions = actions)
		}

		hub.career?.let { career ->
			SummaryCareerView(career = career, actions = actions)
		}

		hub.statistics?.let { statistics ->
			SummaryStatisticsSectionView(item = statistics, actions = actions)
		}
	}
}
