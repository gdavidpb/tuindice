package com.gdavidpb.tuindice.summary.ui.share

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.SuccessResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// The prefetch of a remote artwork brings its picture to disk and never to memory: it yields a stand-in that the
// card would take for the picture. The control is the same request with the memory allowed, which shows that the
// stand-in does reach memory when nothing forbids it. The network is a mock engine that answers with one picture.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ShareableTermCardArtworkPrefetchTest {
	private val context = RuntimeEnvironment.getApplication()

	@Test
	@OptIn(ExperimentalCoilApi::class)
	fun thePrefetchLeavesNothingInMemoryWhileTheSameRequestWithMemoryAllowedDoesReachIt() {
		runBlocking {
			val loader = ImageLoader.Builder(context)
				.components { add(KtorNetworkFetcherFactory(mockPictureClient())) }
				.build()
			val prefetch = remoteArtPrefetchRequest(platformContext = context, url = URL)

			assertIs<SuccessResult>(loader.execute(prefetch))

			assertNull(loader.memoryCache?.get(MemoryCache.Key(URL)))

			assertIs<SuccessResult>(
				loader.execute(prefetch.newBuilder().memoryCachePolicy(CachePolicy.ENABLED).build())
			)

			assertNotNull(loader.memoryCache?.get(MemoryCache.Key(URL)))
		}
	}

	private fun mockPictureClient(): HttpClient {
		val engine = MockEngine {
			respond(
				content = PICTURE_BYTES,
				status = HttpStatusCode.OK,
				headers = headersOf(HttpHeaders.ContentType, "image/png")
			)
		}

		return HttpClient(engine)
	}

	private companion object {
		const val URL = "https://example.com/background.png"

		// A one-pixel picture, PNG.
		val PICTURE_BYTES: ByteArray = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
		)
	}
}
