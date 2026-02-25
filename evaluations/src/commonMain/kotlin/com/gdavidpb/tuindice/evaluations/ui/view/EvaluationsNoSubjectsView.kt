package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.EmptyView

@Composable
fun EvaluationsNoSubjectsView(
	title: String,
	message: String,
	headerContent: @Composable () -> Unit = {}
) {
	EmptyView(
		title = title,
		message = message,
		headerContent = headerContent
	)
}
