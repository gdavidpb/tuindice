package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.text_sync_failed
import tuindice.summary.generated.resources.text_sync_outdated_credentials

@Composable
fun SummaryContentView(
	state: Summary.State.Content,
	syncStatus: SyncStatus,
	summaryItems: List<SummaryItem>,
	onEditProfilePictureClick: () -> Unit
) {
	val profilePictureState = rememberProfilePictureState(
		url = state.profilePictureUrl,
		isLoading = state.isProfilePictureLoading
	)
	val statusIcon = when (syncStatus) {
		SyncStatus.Healthy -> Icons.Outlined.Sync
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials -> Icons.Outlined.SyncProblem
	}
	val statusTint = when (syncStatus) {
		SyncStatus.Healthy -> MaterialTheme.colorScheme.onSurfaceVariant
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials -> MaterialTheme.colorScheme.error
	}
	val statusText = when (syncStatus) {
		SyncStatus.Healthy -> state.lastUpdate
		SyncStatus.Failed -> stringResource(Res.string.text_sync_failed, state.lastUpdate)
		SyncStatus.OutdatedCredentials -> stringResource(Res.string.text_sync_outdated_credentials)
	}

	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.ContentContainer)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		ProfilePictureView(
			state = profilePictureState.value,
			onLoading = { isLoading ->
				profilePictureState.value = profilePictureState.value.copy(
					isLoading = isLoading || state.isProfilePictureLoading
				)
			},
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
				.padding(vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.testTag(SummaryUiTags.StatusIcon)
					.padding(horizontal = 4.dp),
				imageVector = statusIcon,
				tint = statusTint,
				contentDescription = null
			)

			Text(
				modifier = Modifier.testTag(SummaryUiTags.StatusText),
				text = statusText,
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
