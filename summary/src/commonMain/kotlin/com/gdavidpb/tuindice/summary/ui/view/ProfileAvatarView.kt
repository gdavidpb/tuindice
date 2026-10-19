package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.gdavidpb.tuindice.summary.presentation.mapper.profilePictureCacheKey
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The round avatar of the student: the stored profile picture over a neutral person icon in the theme colours, which is
 * what shows when there is no picture ([pictureUrl] is blank), while it loads and when it cannot be loaded. It loads
 * the picture through the app-wide image loader under the key the summary uses, so it finds the bitmap the summary
 * already cached instead of asking for it again. It is decorative: whoever holds it gives it a description.
 */
@Composable
fun ProfileAvatarView(
	pictureUrl: String,
	pictureVersion: Int,
	size: Dp,
	modifier: Modifier = Modifier
) {
	val platformContext = LocalPlatformContext.current
	val imageLoader = remember(platformContext) { SingletonImageLoader.get(platformContext) }
	val request = remember(platformContext, pictureUrl, pictureVersion) {
		val cacheKey = profilePictureCacheKey(url = pictureUrl, version = pictureVersion)

		ImageRequest.Builder(platformContext)
			.data(pictureUrl)
			.memoryCacheKey(cacheKey)
			.diskCacheKey(cacheKey)
			.build()
	}

	Box(
		modifier = modifier
			.size(size)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.surfaceVariant),
		contentAlignment = Alignment.Center
	) {
		Icon(
			modifier = Modifier
				.testTag(SummaryUiTags.ProfileAvatarFallback)
				.size(size / FALLBACK_ICON_DIVISOR),
			imageVector = Icons.Outlined.Person,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)

		if (pictureUrl.isNotBlank()) {
			AsyncImage(
				modifier = Modifier
					.testTag(SummaryUiTags.ProfileAvatarImage)
					.fillMaxSize(),
				model = request,
				imageLoader = imageLoader,
				contentDescription = null,
				contentScale = ContentScale.Crop
			)
		}
	}
}

// The person icon takes this share of the circle's side: the glyph sits inside the circle with room around it.
private const val FALLBACK_ICON_DIVISOR = 1.7f
