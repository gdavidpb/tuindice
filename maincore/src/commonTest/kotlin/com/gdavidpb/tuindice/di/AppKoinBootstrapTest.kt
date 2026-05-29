package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AppKoinBootstrapTest {
	@Test
	fun startAppKoin_startsUsageDataCollectionControllersAfterKoinStarts() {
		val firstController = RecordingUsageDataCollectionController()
		val secondController = RecordingUsageDataCollectionController()

		withStartedKoin(
			start = {
				startAppKoin(
					AppKoinBootstrapRequest(
						platformBootstrap = object : PlatformKoinBootstrap {
							override fun platformModules() = listOf(
								module {
									single<UsageDataCollectionController>(named("first")) {
										firstController
									}
									single<UsageDataCollectionController>(named("second")) {
										secondController
									}
								}
							)
						}
					)
				)
			}
		) {
			assertEquals(1, firstController.starts)
			assertEquals(1, secondController.starts)
		}
	}
}

private class RecordingUsageDataCollectionController : UsageDataCollectionController {
	var starts = 0
		private set

	override fun start() {
		starts += 1
	}
}
