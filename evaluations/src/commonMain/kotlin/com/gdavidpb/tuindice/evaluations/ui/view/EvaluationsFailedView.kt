package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.ErrorView

@Composable
fun EvaluationsFailedView(
	title: String,
	message: String,
	retryText: String? = null,
	onRetryClick: () -> Unit = {},
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
