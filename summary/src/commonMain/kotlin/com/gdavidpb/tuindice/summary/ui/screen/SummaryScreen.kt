package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.view.SummaryContentView
import com.gdavidpb.tuindice.summary.ui.view.SummaryFailedView
import com.gdavidpb.tuindice.summary.ui.view.SummaryLoadingView
import com.gdavidpb.tuindice.summary.ui.view.rememberSummaryItems
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_failed_message
import tuindice.summary.generated.resources.summary_failed_retry
import tuindice.summary.generated.resources.summary_failed_title

@Composable
fun SummaryScreen(
	state: Summary.State,
	syncStatus: SyncStatus,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit
) {
	SealedCrossfade(targetState = state) { targetState ->
		when (targetState) {
			is Summary.State.Loading ->
				SummaryLoadingView()

			is Summary.State.Failed ->
				SummaryFailedView(
					title = stringResource(Res.string.summary_failed_title),
					message = stringResource(Res.string.summary_failed_message),
					retryText = stringResource(Res.string.summary_failed_retry),
					onRetryClick = onRetryClick,
					headerContent = {
						ErrorStateAnimationView()
					}
				)

			is Summary.State.Content ->
				SummaryContentView(
					state = targetState,
					syncStatus = syncStatus,
					summaryItems = rememberSummaryItems(
						state = targetState
					),
					onEditProfilePictureClick = onEditProfilePictureClick
				)
		}
	}
}
