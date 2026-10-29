package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.presentation.mapper.cardUrlsToPrefetch
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem

/**
 * Brings the card pictures of the [backgrounds] to disk while the sheet is open, one after the other and nearest to the
 * chosen one first, so that choosing another background no longer waits for its picture to come over the network. The
 * pictures are neither decoded nor kept in memory here: the card decodes the one it shows when it is chosen. A picture
 * already on disk costs nothing, the walk starts once for the catalog and not again as the choice moves, and closing
 * the sheet stops it.
 */
@Composable
fun ShareTermCardArtPrefetch(backgrounds: List<ShareTermBackgroundItem>) {
	val platformContext = LocalPlatformContext.current
	// The app-wide loader, the same one the card reads: the pictures land in the disk cache the card looks in.
	val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
	val latestBackgrounds = rememberUpdatedState(backgrounds)
	val catalog = backgrounds.map { item -> item.artwork.id }

	LaunchedEffect(catalog) {
		latestBackgrounds.value.cardUrlsToPrefetch().forEach { url ->
			imageLoader.execute(remoteArtPrefetchRequest(platformContext = platformContext, url = url))
		}
	}
}
