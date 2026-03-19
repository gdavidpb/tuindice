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
	state: ProfilePictureState,
	onLoading: (isLoading: Boolean) -> Unit,
	onClick: () -> Unit
) {
	val platformContext = LocalPlatformContext.current
	val placeholderPainter = painterResource(Res.drawable.il_profile_picture_placeholder_owl)
	val imageLoader = remember(platformContext) {
		ImageLoader.Builder(platformContext)
			.components {
				add(KtorNetworkFetcherFactory())
			}
			.build()
	}

	LaunchedEffect(state.url) {
		if (state.url.isBlank()) {
			onLoading(false)
		}
	}

	Box(
		modifier = modifier
			.testTag(SummaryUiTags.ProfilePictureContainer)
			.clickable(
				enabled = isEnabled && !state.isLoading,
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
			AsyncImage(
				modifier = Modifier
					.testTag(SummaryUiTags.ProfilePicturePlaceholderIcon)
					.fillMaxSize(),
				model = ImageRequest.Builder(platformContext)
					.data(state.url.takeIf { it.isNotBlank() })
					.crossfade(true)
					.build(),
				imageLoader = imageLoader,
				placeholder = placeholderPainter,
				error = placeholderPainter,
				fallback = placeholderPainter,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				onLoading = { onLoading(true) },
				onSuccess = { onLoading(false) },
				onError = { onLoading(false) },
			)
		}

		IconButton(
			modifier = Modifier
				.testTag(SummaryUiTags.ProfilePictureEditButton)
				.size(42.dp)
				.align(Alignment.BottomEnd),
			enabled = isEnabled && !state.isLoading,
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
			visible = state.isLoading
		) {
			CircularProgressIndicator(
				modifier = Modifier.testTag(SummaryUiTags.ProfilePictureLoadingIndicator)
			)
		}
	}
}
