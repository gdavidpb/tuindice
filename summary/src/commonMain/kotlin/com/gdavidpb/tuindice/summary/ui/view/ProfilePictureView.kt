package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.painterResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.il_profile_picture_placeholder_owl

@Composable
fun ProfilePictureView(
	modifier: Modifier = Modifier,
	isEnabled: Boolean = true,
	url: String,
	isLoading: Boolean,
	onClick: () -> Unit
) {
	val platformContext = LocalPlatformContext.current
	val placeholderPainter = painterResource(Res.drawable.il_profile_picture_placeholder_owl)
	val imageData = url.takeIf { it.isNotBlank() }
	val imageLoader = remember(platformContext) {
		ImageLoader.Builder(platformContext)
			.components {
				add(KtorNetworkFetcherFactory())
			}
			.build()
	}
	val imageRequest = remember(platformContext, imageData) {
		ImageRequest.Builder(platformContext)
			.data(imageData)
			.crossfade(true)
			.build()
	}
	val lastSuccessfulImageData = remember {
		mutableStateOf<String?>(null)
	}
	val previousImageData = lastSuccessfulImageData.value
	val previousImageRequest = remember(platformContext, previousImageData) {
		previousImageData?.let { data ->
			ImageRequest.Builder(platformContext)
				.data(data)
				.crossfade(false)
				.build()
		}
	}
	val imageLoadingState = remember(url) {
		mutableStateOf(false)
	}
	val shouldKeepPreviousImageVisible = imageData != null &&
		previousImageData != null &&
		imageData != previousImageData
	val isCurrentlyLoading = isLoading || imageLoadingState.value

	LaunchedEffect(imageData) {
		if (imageData == null) {
			lastSuccessfulImageData.value = null
		}
	}

	Box(
		modifier = modifier
			.testTag(SummaryUiTags.ProfilePictureContainer)
			.clickable(
				enabled = isEnabled && !isCurrentlyLoading,
				onClick = onClick
			)
	) {
		Box(
			modifier = Modifier
				.size(128.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceVariant),
			contentAlignment = Alignment.Center
		) {
			if (shouldKeepPreviousImageVisible && previousImageRequest != null) {
				AsyncImage(
					modifier = Modifier.fillMaxSize(),
					model = previousImageRequest,
					imageLoader = imageLoader,
					contentDescription = null,
					contentScale = ContentScale.Crop
				)
			}

			AsyncImage(
				modifier = Modifier
					.testTag(SummaryUiTags.ProfilePicturePlaceholderIcon)
					.fillMaxSize(),
				model = imageRequest,
				imageLoader = imageLoader,
				placeholder = if (shouldKeepPreviousImageVisible) null else placeholderPainter,
				error = if (shouldKeepPreviousImageVisible) null else placeholderPainter,
				fallback = placeholderPainter,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				onLoading = {
					imageLoadingState.value = true
				},
				onSuccess = {
					imageLoadingState.value = false
					lastSuccessfulImageData.value = imageData
				},
				onError = {
					imageLoadingState.value = false
				},
			)
		}

		IconButton(
			modifier = Modifier
				.testTag(SummaryUiTags.ProfilePictureEditButton)
				.size(42.dp)
				.align(Alignment.BottomEnd),
			enabled = isEnabled && !isCurrentlyLoading,
			colors = IconButtonDefaults.filledIconButtonColors(),
			onClick = onClick
		) {
			Icon(
				modifier = Modifier
					.padding(4.dp),
				imageVector = Icons.Outlined.Edit,
				tint = MaterialTheme.colorScheme.onPrimary,
				contentDescription = null
			)
		}

		AnimatedVisibility(
			modifier = Modifier.align(Alignment.Center),
			visible = isCurrentlyLoading
		) {
			CircularProgressIndicator(
				modifier = Modifier.testTag(SummaryUiTags.ProfilePictureLoadingIndicator)
			)
		}
	}
}
