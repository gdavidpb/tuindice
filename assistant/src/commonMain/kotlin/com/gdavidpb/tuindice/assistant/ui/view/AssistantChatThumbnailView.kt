package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size

// A photo's bytes drawn as an image. Coil decodes them off the main thread and keeps the decoded image in its memory
// cache under [cacheKey], which names the photo while it is shown: the tray keys it by its draft id, the bubble by its
// message and its place. The bytes are not written to Coil's disk cache, so a photo the student sent stays off the
// disk. A photo shown whole ([ContentScale.Fit], the dialog) is decoded at its own size.
@Composable
fun AssistantChatThumbnailView(
	bytes: ByteArray?,
	cacheKey: String,
	contentDescription: String?,
	modifier: Modifier = Modifier,
	contentScale: ContentScale = ContentScale.Crop
) {
	if (bytes == null) return

	val platformContext = LocalPlatformContext.current
	val wholePhoto = contentScale == ContentScale.Fit
	val request = remember(cacheKey, bytes, wholePhoto) {
		ImageRequest.Builder(platformContext)
			.data(bytes)
			.memoryCacheKey(cacheKey)
			.diskCachePolicy(CachePolicy.DISABLED)
			.apply { if (wholePhoto) size(Size.ORIGINAL) }
			.build()
	}

	AsyncImage(
		model = request,
		contentDescription = contentDescription,
		modifier = modifier.fillMaxSize(),
		contentScale = contentScale
	)
}
