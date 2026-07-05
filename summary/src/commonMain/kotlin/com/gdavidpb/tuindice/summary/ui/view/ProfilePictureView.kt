package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.ProfilePictureDisplay
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.il_profile_picture_placeholder_owl

@Composable
fun ProfilePictureView(
	modifier: Modifier = Modifier,
	isEnabled: Boolean = true,
	display: ProfilePictureDisplay,
	onClick: () -> Unit
) {
	val platformContext = LocalPlatformContext.current
	val placeholderPainter = painterResource(Res.drawable.il_profile_picture_placeholder_owl)
	val imageLoader = remember(platformContext) {
		profilePictureImageLoader(platformContext = platformContext)
	}
	val viewState = rememberProfilePictureViewState(display = display)
	val isInteractionEnabled = isEnabled && !display.isUploading && !viewState.isColdLoading

	Box(
		modifier = modifier
			.testTag(SummaryUiTags.ProfilePictureContainer)
			.clickable(
				enabled = isInteractionEnabled,
				onClick = onClick
			)
	) {
		ProfilePictureLayerStack(
			layers = viewState.layers,
			imageLoader = imageLoader,
			placeholderPainter = placeholderPainter
		)

		ProfilePictureEditButton(
			modifier = Modifier.align(Alignment.BottomEnd),
			isEnabled = isInteractionEnabled,
			isUploading = display.isUploading,
			onClick = onClick
		)

		AnimatedVisibility(
			modifier = Modifier.align(Alignment.Center),
			visible = viewState.isColdLoading
		) {
			CircularProgressIndicator(
				modifier = Modifier.testTag(SummaryUiTags.ProfilePictureRemoteLoadingIndicator)
			)
		}
	}
}

@Composable
private fun ProfilePictureLayerStack(
	layers: List<ProfilePictureLayer>,
	imageLoader: ImageLoader,
	placeholderPainter: Painter
) {
	Box(
		modifier = Modifier
			.size(128.dp)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.surfaceVariant),
		contentAlignment = Alignment.Center
	) {
		layers.forEach { layer ->
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

private class ProfilePictureViewState(
	val layers: List<ProfilePictureLayer>,
	val isColdLoading: Boolean
)

private class ProfilePictureLayer(
	val tag: String,
	val request: ImageRequest,
	val showsPlaceholder: Boolean = false,
	val showsFallback: Boolean = false,
	val onState: ((AsyncImagePainter.State) -> Unit)? = null
)

@Composable
private fun rememberProfilePictureViewState(
	display: ProfilePictureDisplay
): ProfilePictureViewState {
	val platformContext = LocalPlatformContext.current
	// Last model that actually rendered; it backs the picture while the remote
	// request swaps identity so the swap itself never flashes a placeholder.
	val lastShownImageData = remember {
		mutableStateOf<String?>(null)
	}
	val isRemoteImageLoading = remember(display.url, display.cacheVersion) {
		mutableStateOf(false)
	}
	val presentation = remember(platformContext, display, lastShownImageData.value) {
		profilePictureLayerPresentation(
			platformContext = platformContext,
			display = display,
			lastShownImageData = lastShownImageData,
			isRemoteImageLoading = isRemoteImageLoading
		)
	}

	LaunchedEffect(presentation.remoteImageData) {
		if (presentation.remoteImageData == null) {
			lastShownImageData.value = null
		}
	}

	return ProfilePictureViewState(
		layers = presentation.layers,
		isColdLoading = isRemoteImageLoading.value && !presentation.hasCoveringImage
	)
}

private class ProfilePicturePresentation(
	val remoteImageData: String?,
	val hasCoveringImage: Boolean,
	val layers: List<ProfilePictureLayer>
)

private class ProfilePictureLayerContext(
	val platformContext: PlatformContext,
	val remoteImageData: String?,
	val remoteCacheKey: String?,
	val previewImageData: String?,
	val lastShownImageData: MutableState<String?>,
	val isRemoteImageLoading: MutableState<Boolean>
) {
	val continuityImageData = lastShownImageData.value?.takeIf { data ->
		remoteImageData != null && data != remoteImageData && data != previewImageData
	}
	val hasCoveringImage = previewImageData != null || continuityImageData != null
}

// Bottom-to-top stack: continuity (last rendered pixels), the remote picture, and
// the optimistic local preview; upper layers drop their placeholders so a covered
// load never flashes the owl.
private fun profilePictureLayerPresentation(
	platformContext: PlatformContext,
	display: ProfilePictureDisplay,
	lastShownImageData: MutableState<String?>,
	isRemoteImageLoading: MutableState<Boolean>
): ProfilePicturePresentation {
	val remoteImageData = display.url.takeIf { it.isNotBlank() }
	val layerContext = ProfilePictureLayerContext(
		platformContext = platformContext,
		remoteImageData = remoteImageData,
		// The version keys the cache identity so a re-upload behind a stable URL
		// still invalidates the previously cached bitmap.
		remoteCacheKey = remoteImageData?.let { data -> "$data#v${display.cacheVersion}" },
		previewImageData = display.localPreviewPath?.takeIf { it.isNotBlank() },
		lastShownImageData = lastShownImageData,
		isRemoteImageLoading = isRemoteImageLoading
	)

	return ProfilePicturePresentation(
		remoteImageData = layerContext.remoteImageData,
		hasCoveringImage = layerContext.hasCoveringImage,
		layers = listOfNotNull(
			layerContext.continuityLayer(),
			layerContext.remoteLayer(),
			layerContext.previewLayer()
		)
	)
}

private fun ProfilePictureLayerContext.continuityLayer(): ProfilePictureLayer? {
	val imageData = continuityImageData ?: return null

	return ProfilePictureLayer(
		tag = SummaryUiTags.ProfilePictureContinuityImage,
		request = ImageRequest.Builder(platformContext)
			.data(imageData)
			.crossfade(false)
			.build()
	)
}

private fun ProfilePictureLayerContext.remoteLayer(): ProfilePictureLayer {
	return ProfilePictureLayer(
		tag = SummaryUiTags.ProfilePicturePlaceholderIcon,
		request = ImageRequest.Builder(platformContext)
			.data(remoteImageData)
			.memoryCacheKey(remoteCacheKey)
			.diskCacheKey(remoteCacheKey)
			.crossfade(true)
			.build(),
		showsPlaceholder = !hasCoveringImage,
		showsFallback = previewImageData == null,
		onState = { state ->
			when (state) {
				is AsyncImagePainter.State.Loading ->
					isRemoteImageLoading.value = true

				is AsyncImagePainter.State.Success -> {
					isRemoteImageLoading.value = false
					lastShownImageData.value = remoteImageData
				}

				is AsyncImagePainter.State.Error ->
					isRemoteImageLoading.value = false

				else -> Unit
			}
		}
	)
}

private fun ProfilePictureLayerContext.previewLayer(): ProfilePictureLayer? {
	val imageData = previewImageData ?: return null

	return ProfilePictureLayer(
		tag = SummaryUiTags.ProfilePicturePreviewImage,
		request = ImageRequest.Builder(platformContext)
			.data(imageData)
			.crossfade(true)
			.build(),
		onState = { state ->
			if (state is AsyncImagePainter.State.Success) {
				lastShownImageData.value = imageData
			}
		}
	)
}

// Configured once and shared app-wide so the memory cache survives recomposition
// and navigation; a per-composition loader re-downloads the avatar on every visit.
private fun profilePictureImageLoader(platformContext: PlatformContext): ImageLoader {
	SingletonImageLoader.setSafe { context ->
		ImageLoader.Builder(context)
			.components {
				add(KtorNetworkFetcherFactory())
			}
			.build()
	}

	return SingletonImageLoader.get(platformContext)
}
