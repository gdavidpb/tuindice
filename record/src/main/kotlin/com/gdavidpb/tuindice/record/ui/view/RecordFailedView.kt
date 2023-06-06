package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.ErrorView

@Composable
fun RecordFailedView(
	onRetryClick: () -> Unit
) {
	ErrorView(onRetryClick)
}