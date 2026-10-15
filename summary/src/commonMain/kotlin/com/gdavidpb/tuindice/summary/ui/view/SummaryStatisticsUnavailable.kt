package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The record is not ready to be read: the screen says so and offers nothing to retry, since it does not retry.
@Composable
fun SummaryStatisticsUnavailable(
	title: String,
	message: String
) {
	IllustratedMessageView(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsUnavailable)
			.fillMaxSize()
			.padding(
				start = TuIndiceSpacing.Screen,
				top = InternalScreenDefaults.TopBarSpacing + TuIndiceSpacing.Dialog,
				end = TuIndiceSpacing.Screen,
				bottom = TuIndiceSpacing.Dialog
			),
		title = title,
		message = message,
		titleTestTag = SummaryUiTags.StatisticsUnavailableTitle,
		messageTestTag = SummaryUiTags.StatisticsUnavailableMessage,
		titleStyle = MaterialTheme.typography.headlineSmall,
		headerContent = { EmptyStateAnimationView() }
	)
}
