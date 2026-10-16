package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage

/**
 * The circle of the profile picture: its [layers] stacked one over the other, each keyed by its tag, with the
 * [placeholderPainter] for the layers that show it, and [contentDescription] for a screen reader.
 */
@Composable
fun ProfilePictureLayerStack(
	layers: List<ProfilePictureLayer>,
	imageLoader: ImageLoader,
	placeholderPainter: Painter,
	contentDescription: String
) {
	Box(
		modifier = Modifier
			.size(128.dp)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.semantics { this.contentDescription = contentDescription },
		contentAlignment = Alignment.Center
	) {
		layers.forEach { layer ->
			// Keyed by role so a layer entering or leaving the stack never re-binds a sibling's painter to a different request
			// mid-transition.
			key(layer.tag) {
				AsyncImage(
					modifier = Modifier
						.testTag(layer.tag)
						.fillMaxSize(),
					model = layer.request,
					imageLoader = imageLoader,
					placeholder = if (layer.showsPlaceholder) placeholderPainter else null,
					error = if (layer.showsPlaceholder) placeholderPainter else null,
					fallback = if (layer.showsFallback) placeholderPainter else null,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					onLoading = layer.onState?.let { onState -> { state -> onState(state) } },
					onSuccess = layer.onState?.let { onState -> { state -> onState(state) } },
					onError = layer.onState?.let { onState -> { state -> onState(state) } }
				)
			}
		}
	}
}
