package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

interface ProfilePictureViewRenderer {
	@Composable
	fun Render(
		modifier: Modifier,
		state: ProfilePictureState,
		onLoading: (isLoading: Boolean) -> Unit,
		onClick: () -> Unit
	)
}

@Composable
fun ProfilePictureView(
	modifier: Modifier = Modifier,
	state: ProfilePictureState,
	onLoading: (isLoading: Boolean) -> Unit,
	onClick: () -> Unit,
	renderer: ProfilePictureViewRenderer = koinInject()
) {
	renderer.Render(
		modifier = modifier,
		state = state,
		onLoading = onLoading,
		onClick = onClick
	)
}
