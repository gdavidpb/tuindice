package com.gdavidpb.tuindice.summary.testing

import coil3.Canvas
import coil3.EventListener
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.Uri
import coil3.annotation.DelicateCoilApi
import coil3.decode.DataSource
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * The app-wide image loader of a picture test, with no network: it records every URL fetched and every request started,
 * and serves a small image from a memory cache like the real one. A URL that [failingUrls] names fails, after
 * [failingGate] when it is given, as a signed URL that expired does.
 */
class RecordingProfilePictureLoader(
	private val failingUrls: Set<String> = emptySet(),
	private val failingGate: CompletableDeferred<Unit>? = null
) {
	// Coil fetches off the main thread: the records are flows, whose update is atomic.
	private val fetched = MutableStateFlow(emptyList<String>())
	private val started = MutableStateFlow(emptyList<String>())

	val fetchedUrls: List<String>
		get() = fetched.value

	val startedRequests: List<String>
		get() = started.value

	@OptIn(DelicateCoilApi::class)
	fun install() {
		SingletonImageLoader.setUnsafe { context ->
			ImageLoader.Builder(context)
				.memoryCache { MemoryCache.Builder().maxSizeBytes(MEMORY_CACHE_BYTES).build() }
				.diskCache(null)
				.eventListener(
					object : EventListener() {
						override fun onStart(request: ImageRequest) {
							started.update { requests -> requests + request.data.toString() }
						}
					}
				)
				.components {
					add(
						Fetcher.Factory<Uri> { data, _, _ ->
							Fetcher { fetch(url = data.toString()) }
						},
						Uri::class
					)
				}
				.build()
		}
	}

	@OptIn(DelicateCoilApi::class)
	fun uninstall() {
		SingletonImageLoader.reset()
	}

	private suspend fun fetch(url: String): ImageFetchResult {
		fetched.update { urls -> urls + url }

		if (url in failingUrls) {
			failingGate?.await()
			error("The signature of $url expired.")
		}

		return ImageFetchResult(image = PictureImage, isSampled = false, dataSource = DataSource.NETWORK)
	}

	private object PictureImage : Image {
		override val size: Long = 4L * IMAGE_SIDE * IMAGE_SIDE
		override val width: Int = IMAGE_SIDE
		override val height: Int = IMAGE_SIDE
		override val shareable: Boolean = true

		override fun draw(canvas: Canvas) = Unit
	}

	private companion object {
		const val IMAGE_SIDE = 8
		const val MEMORY_CACHE_BYTES = 1_048_576L
	}
}

// The same picture behind a stable path, signed as the backend signs it (V2) at [expires].
fun v2SignedPictureUrl(expires: Long): String {
	return "https://storage.googleapis.com/tu-indice-usb/profile_pictures/uid.jpg" +
		"?GoogleAccessId=svc%40tu-indice-usb.iam.gserviceaccount.com&Expires=$expires&Signature=sig$expires"
}
