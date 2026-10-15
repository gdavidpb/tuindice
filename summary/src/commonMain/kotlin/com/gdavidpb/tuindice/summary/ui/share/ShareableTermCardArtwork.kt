package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt

// The artwork a card can show for this background: the background itself when the app ships it or its remote artwork
// has loaded, and the one the app ships with when the remote artwork cannot be loaded.
internal suspend fun ShareBackground.resolvedBy(
	imageLoader: ImageLoader,
	platformContext: PlatformContext
): ShareBackground {
	val remote = art as? ShareBackgroundArt.Remote ?: return this
	val result = imageLoader.execute(remoteArtRequest(platformContext = platformContext, url = remote.cardUrl))

	return if (result is SuccessResult) this else ShareBackground.included
}

// The request of a remote artwork, keyed by its address: the one the card loads first and the one it draws later are
// the same picture in memory and on disk, and the card does not fade a second time on top of its own transition.
internal fun remoteArtRequest(platformContext: PlatformContext, url: String): ImageRequest {
	return ImageRequest.Builder(platformContext)
		.data(url)
		.memoryCacheKey(url)
		.diskCacheKey(url)
		.crossfade(false)
		.build()
}

// The title in italics, then the author when the work has one: the credit reads as the work is named in the catalogs.
internal fun artCredit(background: ShareBackground): AnnotatedString {
	return buildAnnotatedString {
		withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
			append(background.title)
		}

		background.author?.let { author -> append(" · $author") }
	}
}
