package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureView
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer
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
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit,
	profilePictureViewRenderer: ProfilePictureViewRenderer
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
					summaryItems = rememberSummaryItems(
						state = targetState
					),
					onEditProfilePictureClick = onEditProfilePictureClick,
					profilePictureContent = { profilePictureState, onLoading, onClick ->
						ProfilePictureView(
							state = profilePictureState,
							onLoading = onLoading,
							onClick = onClick,
							renderer = profilePictureViewRenderer
						)
					},
					lastUpdateLeadingContent = { _, isUpdated, rotation ->
						Icon(
							modifier = Modifier
								.padding(horizontal = 4.dp)
								.rotate(rotation),
							imageVector = if (isUpdated) Icons.Outlined.Sync else Icons.Outlined.SyncProblem,
							tint = if (isUpdated) LocalContentColor.current else MaterialTheme.colorScheme.error,
							contentDescription = null
						)
					}
				)
		}
	}
}
