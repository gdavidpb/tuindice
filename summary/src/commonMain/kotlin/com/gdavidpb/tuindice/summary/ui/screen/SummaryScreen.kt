package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
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
	isSyncing: Boolean = false,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit,
	onUpdatePasswordClick: () -> Unit
) {
	val displayedSyncStatusDetails = remember { mutableStateOf<SyncStatus?>(null) }

	SealedCrossfade(targetState = state) { targetState ->
		when (targetState) {
			Summary.State.Idle -> Unit

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
					isSyncing = isSyncing,
					summaryItems = rememberSummaryItems(
						state = targetState
					),
					onEditProfilePictureClick = onEditProfilePictureClick,
					onStatusIconClick = {
						if (syncStatus != SyncStatus.Healthy)
							displayedSyncStatusDetails.value = syncStatus
					}
				)
		}
	}

	displayedSyncStatusDetails.value?.let { currentSyncStatus ->
		SyncStatusInfoContentDialog(
			syncStatus = currentSyncStatus,
			onUpdatePasswordClick = onUpdatePasswordClick,
			onDismissRequest = { displayedSyncStatusDetails.value = null }
		)
	}
}
