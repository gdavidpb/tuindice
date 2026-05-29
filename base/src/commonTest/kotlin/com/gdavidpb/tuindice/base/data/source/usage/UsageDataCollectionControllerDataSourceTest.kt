package com.gdavidpb.tuindice.base.data.source.usage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UsageDataCollectionControllerDataSourceTest {
	@Test
	fun start_setsInitialConsentAndFollowsChanges() = runTest {
		val calls = mutableListOf<Boolean>()
		val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val repository = InMemoryUsageDataConsentRepository(initialValue = false)
		val controller = UsageDataCollectionControllerDataSource(
			usageDataConsentRepository = repository,
			setCollectionEnabled = calls::add,
			scope = scope
		)

		try {
			controller.start()
			repository.setUsageDataCollectionEnabled(true)
			repository.setUsageDataCollectionEnabled(false)

			assertEquals(listOf(false, true, false), calls)
		} finally {
			scope.cancel()
		}
	}

	@Test
	fun start_isIdempotent() = runTest {
		val calls = mutableListOf<Boolean>()
		val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val controller = UsageDataCollectionControllerDataSource(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = true),
			setCollectionEnabled = calls::add,
			scope = scope
		)

		try {
			controller.start()
			controller.start()

			assertEquals(listOf(true), calls)
		} finally {
			scope.cancel()
		}
	}

	@Test
	fun noOpUsageDataCollectionController_startDoesNothing() {
		NoOpUsageDataCollectionController.start()
	}
}
