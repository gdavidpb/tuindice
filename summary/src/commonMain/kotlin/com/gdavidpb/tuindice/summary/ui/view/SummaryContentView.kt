package com.gdavidpb.tuindice.summary.ui.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@Composable
fun SummaryContentView(
	state: Summary.State.Content,
	syncStatus: SyncStatus,
	summaryItems: List<SummaryItem>,
	onEditProfilePictureClick: () -> Unit,
	onStatusIconClick: () -> Unit
) {
	val isProfilePictureInteractionEnabled = !state.isUserRefreshing
	val statusIcon = when (syncStatus) {
		SyncStatus.Healthy -> Icons.Outlined.Sync
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials -> Icons.Outlined.SyncProblem
	}
	val statusTint = when (syncStatus) {
		SyncStatus.Healthy -> MaterialTheme.colorScheme.onSurfaceVariant
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials -> MaterialTheme.colorScheme.error
	}
	val canOpenStatusDetails = syncStatus != SyncStatus.Healthy

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
						.testTag(SummaryUiTags.StatusIcon),
					imageVector = statusIcon,
					tint = statusTint,
					contentDescription = null
				)
			}

			Spacer(modifier = Modifier.width(4.dp))

			Text(
				modifier = Modifier.testTag(SummaryUiTags.StatusText),
				text = state.lastUpdate,
				style = MaterialTheme.typography.bodyMedium
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
