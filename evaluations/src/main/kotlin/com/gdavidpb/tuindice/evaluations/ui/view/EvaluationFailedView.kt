package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.ErrorView

@Composable
fun EvaluationFailedView(
	onRetryClick: () -> Unit
) {
	ErrorView(onRetryClick)
}
