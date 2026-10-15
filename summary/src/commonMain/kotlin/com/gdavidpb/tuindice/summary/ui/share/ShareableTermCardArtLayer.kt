package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_background_laberinto_cromovegetal_card

/**
 * One artwork drawn over the whole card: the one the app ships with, or a remote picture that is reported as ready when
 * it has loaded and as failed when it has not, so that the card can turn to the one it falls back to.
 */
@Composable
fun ShareableTermCardArtLayer(
	artwork: ShareTermArtwork,
	onArtReady: () -> Unit,
	onArtFailed: (ShareTermArtwork) -> Unit
) {
	when (val source = artwork.art) {
		ShareTermArt.Bundled -> {
			LaunchedEffect(artwork) { onArtReady() }

			Image(
				painter = painterResource(Res.drawable.share_background_laberinto_cromovegetal_card),
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		}

		is ShareTermArt.Remote -> {
			val platformContext = LocalPlatformContext.current
			val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
			val request = remember(platformContext, source.cardUrl) {
				remoteArtRequest(platformContext = platformContext, url = source.cardUrl)
			}

			AsyncImage(
				model = request,
				imageLoader = imageLoader,
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				onSuccess = { onArtReady() },
				onError = { onArtFailed(artwork) }
			)
		}
	}
}
