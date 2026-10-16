package com.gdavidpb.tuindice.summary.ui.share

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.decode.BlackholeDecoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import kotlin.math.abs

// Whether the picture of this artwork can be loaded: the one the app ships with always can, and a remote one is loaded
// now through the app-wide loader, so that the card takes its place only once the picture is there.
internal suspend fun ShareTermArtwork.loadsWith(
	imageLoader: ImageLoader,
	platformContext: PlatformContext
): Boolean {
	val remote = art as? ShareTermArt.Remote ?: return true
	val result = imageLoader.execute(remoteArtRequest(platformContext = platformContext, url = remote.cardUrl))

	return result is SuccessResult
}

// The artwork the card shows once this one has been tried: the artwork itself when its picture loaded, and the one it
// falls back to when it did not. Both the preload and a picture that fails come through here, so the fallback is
// decided once.
internal fun ShareTermArtwork.shownAfter(loaded: Boolean): ShareTermArtwork {
	return if (loaded) this else fallback ?: this
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

// The request that brings the picture of a remote artwork to disk ahead of time, under the keys of [remoteArtRequest],
// so the card reads it from there later. It is never decoded: what it yields is a stand-in that must not reach memory,
// where the card would take it for the picture.
@OptIn(ExperimentalCoilApi::class)
internal fun remoteArtPrefetchRequest(platformContext: PlatformContext, url: String): ImageRequest {
	return remoteArtRequest(platformContext = platformContext, url = url)
		.newBuilder()
		.memoryCachePolicy(CachePolicy.DISABLED)
		.decoderFactory(BlackholeDecoder.Factory())
		.build()
}

// The card pictures to bring ahead of time: those of the remote backgrounds the student has not chosen, nearest to the
// chosen one first, so the ones a swipe reaches come before the far ones. The chosen one is loaded by the card itself,
// and the one the app ships with needs no loading.
internal fun List<ShareTermBackgroundItem>.cardUrlsToPrefetch(): List<String> {
	val chosenIndex = indexOfFirst { item -> item.selected }.coerceAtLeast(0)

	return withIndex()
		.filterNot { (_, item) -> item.selected }
		.sortedBy { (index, _) -> abs(index - chosenIndex) }
		.mapNotNull { (_, item) -> (item.artwork.art as? ShareTermArt.Remote)?.cardUrl }
}
