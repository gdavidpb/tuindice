package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
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
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val isProfilePictureInteractionEnabled = !state.isUserRefreshing
	val isStatusRefreshing = isSyncing || state.isUserRefreshing
	val statusIcon = syncStatusIcon(
		syncStatus = syncStatus,
		isStatusRefreshing = isStatusRefreshing
	)
	val statusTint = if (isStatusRefreshing) {
		MaterialTheme.colorScheme.onSurfaceVariant
	} else {
		when (syncStatus) {
			SyncStatus.Healthy -> MaterialTheme.colorScheme.onSurfaceVariant
			SyncStatus.Unavailable,
			SyncStatus.Failed,
			SyncStatus.OutdatedCredentials,
			-> MaterialTheme.colorScheme.error
		}
	}
	val canOpenStatusDetails = syncStatus != SyncStatus.Healthy && !isStatusRefreshing
	val statusText = state.lastUpdate
	val syncRotation = remember { Animatable(0f) }

	LaunchedEffect(isStatusRefreshing, animationsEnabled) {
		if (isStatusRefreshing && animationsEnabled) {
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
	val statusIconRotation = if (animationsEnabled) {
		syncStatusIconRotation(
			isStatusRefreshing = isStatusRefreshing,
			currentRotation = syncRotation.value
		)
	} else {
		0f
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
				.padding(horizontal = 16.dp)
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
						.rotate(statusIconRotation)
						.testTag(SummaryUiTags.StatusIcon),
					imageVector = statusIcon,
					tint = statusTint,
					contentDescription = null
				)
			}

			Spacer(modifier = Modifier.width(4.dp))

			AnimatedSyncStatusText(
				modifier = Modifier.weight(1f, fill = false),
				text = statusText,
			)
		}

		LazyColumn(
			modifier = Modifier.testTag(SummaryUiTags.ItemsList)
		) {
			itemsIndexed(
				items = summaryItems,
				key = { _, item -> item.header },
				contentType = { _, _ -> SummaryItemContentType }
			) { index, item ->
				StatusCardItemView(
					modifier = Modifier.testTag(SummaryUiTags.statusCard(index)),
					header = item.header,
					entries = item.entries
				)
			}
		}
	}
}

internal fun syncStatusIcon(
	syncStatus: SyncStatus,
	isStatusRefreshing: Boolean
) = if (isStatusRefreshing) {
	Icons.Outlined.Sync
} else {
	when (syncStatus) {
		SyncStatus.Healthy -> Icons.Outlined.Sync
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials,
		-> Icons.Outlined.SyncProblem
	}
}

internal fun syncStatusIconRotation(
	isStatusRefreshing: Boolean,
	currentRotation: Float
): Float {
	return if (isStatusRefreshing) currentRotation else 0f
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
private const val SummaryItemContentType = "summary_item"
internal const val SYNC_STATUS_TEXT_ANIMATION_DURATION_MILLIS = TuIndiceAnimation.StandardMillis
