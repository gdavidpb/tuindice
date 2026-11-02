package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.decodeToImageBitmap

// A photo's bytes drawn as an image, decoded once for each [key]. The thumbnails are small, so the decoding stays
// in place: the UI has no dispatcher to hand it to.
@Composable
fun AssistantChatThumbnailView(
	bytes: ByteArray?,
	key: String,
	contentDescription: String?,
	modifier: Modifier = Modifier,
	contentScale: ContentScale = ContentScale.Crop
) {
	val bitmap = remember(key, bytes) { bytes?.decodeToImageBitmap() }

	bitmap?.let { image ->
		Image(
			bitmap = image,
			contentDescription = contentDescription,
			modifier = modifier.fillMaxSize(),
			contentScale = contentScale
		)
	}
}
