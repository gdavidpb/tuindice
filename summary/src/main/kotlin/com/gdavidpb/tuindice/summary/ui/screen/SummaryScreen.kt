package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.view.SummaryFailedView
import com.gdavidpb.tuindice.summary.ui.view.SummaryLoadedView
import com.gdavidpb.tuindice.summary.ui.view.SummaryLoadingView

@Composable
fun SummaryScreen(
	state: Summary.State,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit
) {
	Crossfade(targetState = state) { targetState ->
		when (targetState) {
			is Summary.State.Loading ->
				SummaryLoadingView()

			is Summary.State.Failed ->
				SummaryFailedView(
					onRetryClick = onRetryClick
				)

			is Summary.State.Content ->
				SummaryLoadedView(
					state = targetState,
					onEditProfilePictureClick = onEditProfilePictureClick
				)
		}
	}
}