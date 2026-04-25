package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import kotlin.math.abs
import kotlin.math.ceil

@Composable
fun SummaryContentView(
	state: Summary.State.Content,
	syncStatus: SyncStatus,
	isSyncing: Boolean = false,
	summaryItems: List<SummaryItem>,
	onEditProfilePictureClick: () -> Unit,
	onStatusIconClick: () -> Unit
) {
	val isProfilePictureInteractionEnabled = !state.isUserRefreshing
	val statusIcon = when (syncStatus) {
		SyncStatus.Healthy -> Icons.Outlined.Sync
		SyncStatus.OutdatedCredentials -> Icons.Outlined.SyncProblem
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		-> Icons.Outlined.Sync
	}
	val statusTint = when (syncStatus) {
		SyncStatus.Healthy -> MaterialTheme.colorScheme.onSurfaceVariant
		SyncStatus.OutdatedCredentials -> MaterialTheme.colorScheme.error
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		-> MaterialTheme.colorScheme.onSurfaceVariant
	}
	val canOpenStatusDetails = syncStatus == SyncStatus.OutdatedCredentials
	val syncRotation = remember { Animatable(0f) }

	LaunchedEffect(isSyncing) {
		if (isSyncing) {
			while (true) {
				syncRotation.animateTo(
					targetValue = syncRotation.value - SYNC_ICON_FULL_ROTATION_DEGREES,
					animationSpec = tween(
						durationMillis = SYNC_ICON_ROTATION_DURATION_MILLIS,
						easing = FastOutSlowInEasing
					)
				)
			}
		} else {
			val stopTarget = nextSyncIconStopRotation(syncRotation.value)
			val remainingDegrees = abs(stopTarget - syncRotation.value)

			if (remainingDegrees > 0f) {
				syncRotation.animateTo(
					targetValue = stopTarget,
					animationSpec = tween(
						durationMillis = syncIconStopDurationMillis(remainingDegrees),
						easing = LinearOutSlowInEasing
					)
				)
			}

			syncRotation.snapTo(0f)
		}
	}

	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.ContentContainer)
			.fillMaxSize()
			.padding(top = InternalScreenDefaults.TopBarSpacing + 8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		ProfilePictureView(
			modifier = Modifier,
			isEnabled = isProfilePictureInteractionEnabled,
			url = state.profilePictureUrl,
			isLoading = state.isProfilePictureLoading,
			onClick = onEditProfilePictureClick
		)

		GradeTextView(
			grade = state.grade
		)

		Text(
			modifier = Modifier.testTag(SummaryUiTags.NameText),
			text = state.name,
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.Medium
		)

		Text(
			modifier = Modifier.testTag(SummaryUiTags.CareerText),
			text = state.careerName,
			style = MaterialTheme.typography.bodyMedium
		)

		Row(
			modifier = Modifier
				.testTag(SummaryUiTags.StatusRow)
				.padding(bottom = 8.dp)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				modifier = Modifier
					.size(28.dp)
					.testTag(SummaryUiTags.StatusIconButton),
				enabled = canOpenStatusDetails,
				onClick = onStatusIconClick
			) {
				Icon(
					modifier = Modifier
						.size(20.dp)
						.rotate(syncRotation.value)
						.testTag(SummaryUiTags.StatusIcon),
					imageVector = statusIcon,
					tint = statusTint,
					contentDescription = null
				)
			}

			Spacer(modifier = Modifier.width(4.dp))

			AnimatedSyncStatusText(
				text = state.lastUpdate,
			)
		}

		LazyColumn(
			modifier = Modifier.testTag(SummaryUiTags.ItemsList)
		) {
			itemsIndexed(items = summaryItems) { index, item ->
				StatusCardItemView(
					modifier = Modifier.testTag(SummaryUiTags.statusCard(index)),
					header = item.header,
					entries = item.entries
				)
			}
		}
	}
}

@Composable
private fun AnimatedSyncStatusText(
	text: String,
	modifier: Modifier = Modifier
) {
	AnimatedContent(
		targetState = text,
		transitionSpec = {
			val enter = fadeIn(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				)
			) + slideInVertically(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				),
				initialOffsetY = { height -> height / 3 }
			)
			val exit = fadeOut(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				)
			) + slideOutVertically(
				animationSpec = tween(
					durationMillis = SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS,
					easing = FastOutSlowInEasing
				),
				targetOffsetY = { height -> -height / 3 }
			)

			enter togetherWith exit
		},
		label = "SummarySyncStatusTextAnimatedContent"
	) { targetText ->
		Text(
			modifier = modifier.testTag(SummaryUiTags.StatusText),
			text = targetText,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

private fun nextSyncIconStopRotation(currentRotation: Float): Float {
	val nextTurn = ceil((-currentRotation / SYNC_ICON_FULL_ROTATION_DEGREES).toDouble()).toFloat()
	return -nextTurn * SYNC_ICON_FULL_ROTATION_DEGREES
}

private fun syncIconStopDurationMillis(remainingDegrees: Float): Int {
	return (SYNC_ICON_ROTATION_DURATION_MILLIS * (remainingDegrees / SYNC_ICON_FULL_ROTATION_DEGREES))
		.toInt()
		.coerceIn(
			minimumValue = SYNC_ICON_MIN_STOP_DURATION_MILLIS,
			maximumValue = SYNC_ICON_ROTATION_DURATION_MILLIS
		)
}

private const val SYNC_ICON_ROTATION_DURATION_MILLIS = 900
private const val SYNC_ICON_MIN_STOP_DURATION_MILLIS = 180
private const val SYNC_ICON_FULL_ROTATION_DEGREES = 360f
private const val SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS = 220
