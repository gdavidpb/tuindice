package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.summary.presentation.contract.Summary

@Composable
fun SummaryScreen(
	state: Summary.State,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit,
	loadingContent: @Composable () -> Unit,
	failedContent: @Composable (onRetryClick: () -> Unit) -> Unit,
	contentStateContent: @Composable (
		state: Summary.State.Content,
		onEditProfilePictureClick: () -> Unit
	) -> Unit
) {
	SealedCrossfade(targetState = state) { targetState ->
		when (targetState) {
			is Summary.State.Loading ->
				loadingContent()

			is Summary.State.Failed ->
				failedContent(onRetryClick)

			is Summary.State.Content ->
				contentStateContent(
					targetState,
					onEditProfilePictureClick
				)
		}
	}
}
