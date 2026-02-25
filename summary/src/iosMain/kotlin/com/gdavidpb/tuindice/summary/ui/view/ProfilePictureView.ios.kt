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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode
import org.koin.compose.koinInject

class IosProfilePictureViewRenderer : ProfilePictureViewRenderer {
	@Composable
	override fun Render(
		modifier: Modifier,
		state: ProfilePictureState,
		onLoading: (isLoading: Boolean) -> Unit,
		onClick: () -> Unit
	) {
		val httpClient = koinInject<HttpClient>()
		var remoteImage by remember(state.url) {
			mutableStateOf<UIImage?>(null)
		}

		LaunchedEffect(state.url) {
			val url = state.url.takeIf { value -> value.isNotBlank() }
			if (url == null) {
				remoteImage = null
				onLoading(false)
				return@LaunchedEffect
			}

			onLoading(true)
			remoteImage = loadRemoteImage(
				httpClient = httpClient,
				url = url
			)
			onLoading(false)
		}

		Box(
			modifier = modifier
				.clickable { if (!state.isLoading) onClick() }
		) {
			Box(
				modifier = Modifier
					.size(120.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surfaceVariant),
				contentAlignment = Alignment.Center
			) {
				if (remoteImage == null) {
					Icon(
						imageVector = Icons.Outlined.Person,
						contentDescription = null,
						tint = if (state.url.isBlank())
							MaterialTheme.colorScheme.onSurfaceVariant
						else
							MaterialTheme.colorScheme.primary
					)
				} else {
					UIKitView(
						factory = {
							UIImageView().apply {
								contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
								clipsToBounds = true
							}
						},
						update = { imageView ->
							imageView.image = remoteImage
						},
						modifier = Modifier.fillMaxSize()
					)
				}
			}

			IconButton(
				modifier = Modifier
					.size(32.dp)
					.align(Alignment.BottomEnd),
				enabled = !state.isLoading,
				colors = IconButtonDefaults.filledIconButtonColors(),
				onClick = onClick
			) {
				Icon(
					modifier = Modifier.padding(4.dp),
					imageVector = Icons.Outlined.Edit,
					tint = MaterialTheme.colorScheme.onPrimary,
					contentDescription = null
				)
			}

			AnimatedVisibility(
				modifier = Modifier.align(Alignment.Center),
				visible = state.isLoading
			) {
				CircularProgressIndicator()
			}
		}
	}
}

private suspend fun loadRemoteImage(
	httpClient: HttpClient,
	url: String
): UIImage? {
	return runCatching {
		val imageData: ByteArray = httpClient.get(url).body()
		val cacheDirectory = "${NSTemporaryDirectory().trimEnd('/')}/tuindice/profile-picture-cache".toPath()
		FileSystem.SYSTEM.createDirectories(cacheDirectory)

		val cachePath = cacheDirectory.resolve("${url.hashCode().toString(16)}.img")
		FileSystem.SYSTEM.write(cachePath) {
			write(imageData)
		}

		UIImage.imageWithContentsOfFile(cachePath.toString())
	}.getOrNull()
}
