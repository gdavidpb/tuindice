package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsTab
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluations_tab_history
import tuindice.evaluations.generated.resources.evaluations_tab_upcoming

@Composable
fun EvaluationsTabsView(
	selectedTab: EvaluationsTab,
	onTabClick: (EvaluationsTab) -> Unit
) {
	Row(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationsTabRow)
			.fillMaxWidth()
			.padding(horizontal = 24.dp),
		horizontalArrangement = Arrangement.spacedBy(32.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		EvaluationsTabTextView(
			modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsUpcomingTab),
			text = stringResource(Res.string.evaluations_tab_upcoming),
			selected = selectedTab == EvaluationsTab.Upcoming,
			onClick = { onTabClick(EvaluationsTab.Upcoming) }
		)

		EvaluationsTabTextView(
			modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsHistoryTab),
			text = stringResource(Res.string.evaluations_tab_history),
			selected = selectedTab == EvaluationsTab.History,
			onClick = { onTabClick(EvaluationsTab.History) }
		)
	}
}
