package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AppKoinBootstrapTest {
	@Test
	fun startAppKoin_startsAppStartupTasksAfterKoinStarts() {
		val firstTask = RecordingAppStartupTask()
		val secondTask = RecordingAppStartupTask()

		withStartedKoin(
			start = {
				startAppKoin(
					AppKoinBootstrapRequest(
						platformBootstrap = object : PlatformKoinBootstrap {
							override fun platformModules() = listOf(
								module {
									single<AppStartupTask>(named("first")) {
										firstTask
									}
									single<AppStartupTask>(named("second")) {
										secondTask
									}
								}
							)
						}
					)
				)
			}
		) {
			assertEquals(1, firstTask.starts)
			assertEquals(1, secondTask.starts)
		}
	}
}

private class RecordingAppStartupTask : AppStartupTask {
	var starts = 0
		private set

	override fun start() {
		starts += 1
	}
}
