package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

interface ProfilePictureViewRenderer {
	@Composable
	fun Render(
		modifier: Modifier,
		url: String,
		onLoading: (isLoading: Boolean) -> Unit
	)
}

@Composable
fun ProfilePictureView(
	modifier: Modifier = Modifier,
	state: ProfilePictureState,
	onLoading: (isLoading: Boolean) -> Unit,
	onClick: () -> Unit,
	renderer: ProfilePictureViewRenderer
) {
	Box(
		modifier = modifier
			.clickable { if (!state.isLoading) onClick() }
	) {
		Box(
			modifier = Modifier
				.size(120.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceVariant),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Outlined.Person,
				contentDescription = null,
				tint = if (state.url.isBlank())
					MaterialTheme.colorScheme.onSurfaceVariant
				else
					MaterialTheme.colorScheme.primary
			)

			renderer.Render(
				modifier = Modifier.fillMaxSize(),
				url = state.url,
				onLoading = onLoading
			)
		}

		IconButton(
			modifier = Modifier
				.size(32.dp)
				.align(Alignment.BottomEnd),
			enabled = !state.isLoading,
			colors = IconButtonDefaults.filledIconButtonColors(),
			onClick = onClick
		) {
			Icon(
				modifier = Modifier.padding(4.dp),
				imageVector = Icons.Outlined.Edit,
				tint = MaterialTheme.colorScheme.onPrimary,
				contentDescription = null
			)
		}

		AnimatedVisibility(
			modifier = Modifier.align(Alignment.Center),
			visible = state.isLoading
		) {
			CircularProgressIndicator()
		}
	}
}
