package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt

@Composable
fun RecordFailedView(
	title: String,
	message: String,
	art: RecordFailedArt,
	retryText: String,
	onRetryClick: () -> Unit
) {
	ErrorView(
		title = title,
		message = message,
		retryText = retryText,
		onRetryClick = onRetryClick,
		headerContent = {
			when (art) {
				RecordFailedArt.Error -> ErrorStateAnimationView()
				RecordFailedArt.NoRecord -> EmptyStateAnimationView()
			}
		}
	)
}
