package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode

class IosProfilePictureViewRenderer(
	private val httpClient: HttpClient
) : ProfilePictureViewRenderer {
	@Composable
	override fun Render(
		modifier: Modifier,
		url: String,
		onLoading: (isLoading: Boolean) -> Unit
	) {
		var remoteImage by remember(url) {
			mutableStateOf<UIImage?>(null)
		}

		LaunchedEffect(url) {
			val source = url.takeIf { value -> value.isNotBlank() }
			if (source == null) {
				remoteImage = null
				onLoading(false)
				return@LaunchedEffect
			}

			onLoading(true)
			remoteImage = loadRemoteImage(
				httpClient = httpClient,
				url = source
			)
			onLoading(false)
		}

		if (remoteImage == null) return

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
			modifier = modifier
		)
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
