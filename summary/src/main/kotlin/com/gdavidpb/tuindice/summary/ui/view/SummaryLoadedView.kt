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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.contract.Summary

@Composable
fun SummaryLoadedView(
	state: Summary.State.Content,
	onEditProfilePictureClick: () -> Unit
) {
	var profilePictureState by rememberProfilePictureState(
		url = state.profilePictureUrl,
		isLoading = state.isProfilePictureLoading
	)

	val updatingTransition = rememberInfiniteTransition()

	val updatingAnimation = updatingTransition.animateFloat(
		initialValue = 0f,
		targetValue = -180f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 500,
				easing = LinearEasing
			)
		)
	)

	Column(
		modifier = Modifier
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		ProfilePictureView(
			state = profilePictureState,
			onClick = onEditProfilePictureClick,
			onLoading = { isLoading ->
				profilePictureState = profilePictureState.copy(
					isLoading = isLoading || state.isProfilePictureLoading
				)
			}
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
				.padding(vertical = dimensionResource(id = R.dimen.dp_8)),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.dp_4))
					.rotate(if (state.isUpdating) updatingAnimation.value else 0f),
				imageVector = if (state.isUpdated) Icons.Outlined.Sync else Icons.Outlined.SyncProblem,
				tint = if (state.isUpdated) LocalContentColor.current else MaterialTheme.colorScheme.error,
				contentDescription = null
			)

			Text(
				text = state.lastUpdate,
				style = MaterialTheme.typography.bodyMedium
			)
		}

		val summaryItems = getSummaryItems(state = state)

		LazyColumn {
			items(items = summaryItems) { item ->
				StatusCardView(item)
			}
		}
	}
}