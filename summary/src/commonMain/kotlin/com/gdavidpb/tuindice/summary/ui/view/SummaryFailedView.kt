package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.ErrorView

@Composable
fun SummaryFailedView(
	title: String,
	message: String,
	retryText: String,
	onRetryClick: () -> Unit,
	headerContent: @Composable () -> Unit = {}
) {
	ErrorView(
		title = title,
		message = message,
		retryText = retryText,
		onRetryClick = onRetryClick,
		headerContent = headerContent
	)
}
