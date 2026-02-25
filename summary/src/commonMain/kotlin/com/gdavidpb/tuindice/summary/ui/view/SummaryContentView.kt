package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

@Composable
fun SummaryContentView(
	state: Summary.State.Content,
	summaryItems: List<SummaryItem>,
	onEditProfilePictureClick: () -> Unit,
	profilePictureContent: @Composable (
		state: ProfilePictureState,
		onLoading: (isLoading: Boolean) -> Unit,
		onClick: () -> Unit
	) -> Unit,
	lastUpdateLeadingContent: @Composable (
		isUpdating: Boolean,
		isUpdated: Boolean,
		rotation: Float
	) -> Unit
) {
	var profilePictureState by rememberProfilePictureState(
		url = state.profilePictureUrl,
		isLoading = state.isProfilePictureLoading
	)

	val updatingTransition = rememberInfiniteTransition(
		label = "SummaryContentView_rememberInfiniteTransition"
	)

	val updatingAnimation = updatingTransition.animateFloat(
		initialValue = 0f,
		targetValue = -180f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 500,
				easing = LinearEasing
			)
		),
		label = "SummaryContentView_animateFloat"
	)

	Column(
		modifier = Modifier
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		profilePictureContent(
			profilePictureState,
			{ isLoading ->
				profilePictureState = profilePictureState.copy(
					isLoading = isLoading || state.isProfilePictureLoading
				)
			},
			onEditProfilePictureClick
		)

		GradeTextView(
			grade = state.grade
		)

		Text(
			text = state.name,
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.Medium
		)

		Text(
			text = state.careerName,
			style = MaterialTheme.typography.bodyMedium
		)

		Row(
			modifier = Modifier
				.padding(vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			lastUpdateLeadingContent(
				state.isUpdating,
				state.isUpdated,
				if (state.isUpdating) updatingAnimation.value else 0f
			)

			Text(
				text = state.lastUpdate,
				style = MaterialTheme.typography.bodyMedium
			)
		}

		LazyColumn {
			items(items = summaryItems) { item ->
				StatusCardItemView(
					header = item.header,
					entries = item.entries
				)
			}
		}
	}
}
