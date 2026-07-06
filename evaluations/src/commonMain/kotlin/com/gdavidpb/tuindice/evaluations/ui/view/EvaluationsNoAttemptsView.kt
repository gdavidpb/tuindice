package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.EmptyView

@Composable
fun EvaluationsNoAttemptsView(
	title: String,
	message: String,
	actionLabel: String? = null,
	onActionClick: () -> Unit = {},
	headerContent: @Composable () -> Unit = {}
) {
	EmptyView(
		title = title,
		message = message,
		actionLabel = actionLabel,
		onActionClick = onActionClick,
		headerContent = headerContent
	)
}
