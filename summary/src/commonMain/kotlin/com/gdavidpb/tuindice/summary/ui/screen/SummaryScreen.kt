package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.dialog.SyncStatusInfoContentDialog
import com.gdavidpb.tuindice.summary.ui.view.SummaryContentView
import com.gdavidpb.tuindice.summary.ui.view.SummaryFailedView
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
	syncReport: SyncReport,
	isSyncing: Boolean = false,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit,
	onUpdatePasswordClick: () -> Unit
) {
	val displayedSyncStatusDetails = remember { mutableStateOf<SyncStatusDetails?>(null) }
	val acknowledgedSyncAttentionKey = remember { mutableStateOf<String?>(null) }
	val syncAttentionKey = syncAttentionKey(
		syncStatus = syncStatus,
		syncReport = syncReport
	)
	val shouldShowSyncAttentionHalo = syncAttentionKey != null &&
		acknowledgedSyncAttentionKey.value != syncAttentionKey &&
		!isSyncing

	LaunchedEffect(syncAttentionKey) {
		if (syncAttentionKey == null) {
			acknowledgedSyncAttentionKey.value = null
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		SealedCrossfade(targetState = state) { targetState ->
			when (targetState) {
				Summary.State.Idle -> Unit

				is Summary.State.Loading ->
					LoadingView(indicatorTag = SummaryUiTags.LoadingIndicator)

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
						syncReport = syncReport,
						isSyncing = isSyncing,
						showSyncAttentionHalo = shouldShowSyncAttentionHalo,
						summaryItems = rememberSummaryItems(
							state = targetState
						),
						onEditProfilePictureClick = onEditProfilePictureClick,
						onStatusIconClick = {
							syncAttentionKey?.let { currentKey ->
								acknowledgedSyncAttentionKey.value = currentKey
								displayedSyncStatusDetails.value = SyncStatusDetails(
									status = syncStatus,
									report = syncReport
								)
							}
						}
					)
			}
		}
	}

	displayedSyncStatusDetails.value?.let { currentSyncStatus ->
		SyncStatusInfoContentDialog(
			syncStatus = currentSyncStatus.status,
			syncReport = currentSyncStatus.report,
			onUpdatePasswordClick = onUpdatePasswordClick,
			onDismissRequest = { displayedSyncStatusDetails.value = null }
		)
	}
}

private data class SyncStatusDetails(
	val status: SyncStatus,
	val report: SyncReport
)

private fun syncAttentionKey(
	syncStatus: SyncStatus,
	syncReport: SyncReport
): String? {
	return if (syncStatus != SyncStatus.Healthy || syncReport.hasUnavailableSource) {
		"$syncStatus|$syncReport"
	} else {
		null
	}
}
