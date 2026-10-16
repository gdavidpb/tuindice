package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_background_laberinto_cromovegetal_thumb

/**
 * The thumbnail of one artwork, filling its tile: the bundled picture, or the remote thumbnail. A thumbnail that cannot
 * be loaded stays the tile's plain surface, never another work's picture.
 */
@Composable
fun ShareTermThumbnail(art: ShareTermArt) {
	when (art) {
		ShareTermArt.Bundled ->
			Image(
				painter = painterResource(Res.drawable.share_background_laberinto_cromovegetal_thumb),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)

		is ShareTermArt.Remote -> {
			val platformContext = LocalPlatformContext.current
			// The app-wide loader, the same one the card reads: its memory holds the thumbnails between visits.
			val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }

			AsyncImage(
				model = art.thumbUrl,
				imageLoader = imageLoader,
				contentDescription = null,
				// A thumbnail that cannot be loaded stays the frame's plain surface, never another work's picture.
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.surfaceVariant),
				contentScale = ContentScale.Crop
			)
		}
	}
}
