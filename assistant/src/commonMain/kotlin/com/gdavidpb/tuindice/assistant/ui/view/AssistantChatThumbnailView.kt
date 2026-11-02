package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

// A photo's bytes drawn as an image. The bytes are decoded off the main thread, once for each [key]; nothing is drawn
// until the decoding is done, so the tile keeps its place and background meanwhile.
@Composable
fun AssistantChatThumbnailView(
	bytes: ByteArray?,
	key: String,
	contentDescription: String?,
	modifier: Modifier = Modifier,
	contentScale: ContentScale = ContentScale.Crop
) {
	val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = key, key2 = bytes) {
		value = bytes?.let { content -> withContext(Dispatchers.Default) { content.decodeToImageBitmap() } }
	}

	bitmap?.let { image ->
		Image(
			bitmap = image,
			contentDescription = contentDescription,
			modifier = modifier.fillMaxSize(),
			contentScale = contentScale
		)
	}
}
