package com.gdavidpb.tuindice.summary.data.source

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import kotlin.concurrent.Volatile

/**
 * Says how the app-wide image loader is built, so the avatar it decodes can be dropped with the
 * session. Configured once at start-up and shared: its memory cache survives recomposition and
 * navigation, where a per-composition loader would download the avatar again on every visit.
 * Coil builds the loader on first use; the instance is kept here because it is the only handle to
 * that memory cache that does not need a platform context.
 */
class ProfilePictureImageLoaderDataSource : AppStartupTask, SessionMemory {
	@Volatile
	private var imageLoader: ImageLoader? = null

	override fun start() {
		SingletonImageLoader.setSafe { context ->
			ImageLoader.Builder(context)
				.components {
					add(KtorNetworkFetcherFactory())
				}
				.build()
				.also { loader -> imageLoader = loader }
		}
	}

	// The decoded avatar of the account that is leaving. Its copy on disk goes with the wipe; a
	// loader that was never built has nothing to drop.
	override suspend fun clearSessionMemory() {
		imageLoader?.memoryCache?.clear()
	}
}
