package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.ErrorView

@Composable
fun SummaryFailedView(
	onRetryClick: () -> Unit
) {
	ErrorView(onRetryClick)
}