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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_edit_profile_picture
import tuindice.summary.generated.resources.a11y_profile_picture
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
				onClickLabel = stringResource(Res.string.a11y_edit_profile_picture),
				onClick = onClick
			)
	) {
		ProfilePictureLayerStack(
			layers = viewState.layers,
			imageLoader = imageLoader,
			placeholderPainter = placeholderPainter,
			contentDescription = stringResource(Res.string.a11y_profile_picture)
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
			// Keyed by role so a layer entering or leaving the stack never re-binds a
			// sibling's painter to a different request mid-transition.
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
	val lastShownImage = remember {
		mutableStateOf<ProfilePictureShownImage?>(null)
	}
	val isRemoteImageLoading = remember(display.identity, display.cacheVersion) {
		mutableStateOf(false)
	}
	val presentation = remember(platformContext, display, lastShownImage.value) {
		profilePictureLayerPresentation(
			platformContext = platformContext,
			display = display,
			lastShownImage = lastShownImage,
			isRemoteImageLoading = isRemoteImageLoading
		)
	}

	LaunchedEffect(presentation.remoteImageData) {
		if (presentation.remoteImageData == null) {
			lastShownImage.value = null
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

private data class ProfilePictureShownImage(
	val data: String,
	val cacheKey: String?
)

private class ProfilePictureLayerContext(
	val platformContext: PlatformContext,
	val remoteImageData: String?,
	val remoteCacheKey: String?,
	val previewImageData: String?,
	val lastShownImage: MutableState<ProfilePictureShownImage?>,
	val isRemoteImageLoading: MutableState<Boolean>
) {
	val continuityImage = lastShownImage.value?.takeIf { shown ->
		remoteImageData != null && shown.data != remoteImageData && shown.data != previewImageData
	}
	val hasCoveringImage = previewImageData != null || continuityImage != null
}

// Bottom-to-top stack: continuity (last rendered pixels), the remote picture, and
// the optimistic local preview; upper layers drop their placeholders so a covered
// load never flashes the owl.
private fun profilePictureLayerPresentation(
	platformContext: PlatformContext,
	display: ProfilePictureDisplay,
	lastShownImage: MutableState<ProfilePictureShownImage?>,
	isRemoteImageLoading: MutableState<Boolean>
): ProfilePicturePresentation {
	val remoteImageData = display.url.takeIf { it.isNotBlank() }
	val layerContext = ProfilePictureLayerContext(
		platformContext = platformContext,
		remoteImageData = remoteImageData,
		// The cache identity ignores the rotating signed-URL signature and keys on
		// the version so a re-upload behind a stable path still invalidates the
		// previously cached bitmap, while a signature-only rotation keeps hitting it.
		remoteCacheKey = remoteImageData?.let { "${display.identity}#v${display.cacheVersion}" },
		previewImageData = display.localPreviewPath?.takeIf { it.isNotBlank() },
		lastShownImage = lastShownImage,
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
	val shownImage = continuityImage ?: return null

	return ProfilePictureLayer(
		tag = SummaryUiTags.ProfilePictureContinuityImage,
		request = ImageRequest.Builder(platformContext)
			.data(shownImage.data)
			.memoryCacheKey(shownImage.cacheKey)
			.diskCacheKey(shownImage.cacheKey)
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
					lastShownImage.value = remoteImageData?.let { data ->
						ProfilePictureShownImage(data = data, cacheKey = remoteCacheKey)
					}
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
				lastShownImage.value = ProfilePictureShownImage(data = imageData, cacheKey = null)
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
