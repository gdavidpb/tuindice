package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

class AndroidProfilePictureViewRenderer : ProfilePictureViewRenderer {
	@Composable
	override fun Render(
		modifier: Modifier,
		url: String,
		onLoading: (isLoading: Boolean) -> Unit
	) {
		LaunchedEffect(url) {
			if (url.isBlank()) {
				onLoading(false)
			}
		}

		if (url.isBlank()) return

		AsyncImage(
			modifier = modifier,
			model = ImageRequest.Builder(LocalContext.current)
				.data(url)
				.crossfade(true)
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			onLoading = { onLoading(true) },
			onSuccess = { onLoading(false) },
			onError = { onLoading(false) },
		)
	}
}
