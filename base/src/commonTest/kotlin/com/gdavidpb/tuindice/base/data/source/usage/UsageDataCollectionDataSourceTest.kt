package com.gdavidpb.tuindice.base.data.source.usage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UsageDataCollectionDataSourceTest {
	@Test
	fun start_setsInitialConsentAndFollowsChanges() = runTest {
		val calls = mutableListOf<Boolean>()
		val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val repository = InMemoryUsageDataConsentRepository(initialValue = false)
		val dataSource = UsageDataCollectionDataSource(
			usageDataConsentRepository = repository,
			setCollectionEnabledActions = listOf { enabled -> calls += enabled },
			coroutineScope = scope
		)

		try {
			dataSource.start()
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
		val dataSource = UsageDataCollectionDataSource(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = true),
			setCollectionEnabledActions = listOf { enabled -> calls += enabled },
			coroutineScope = scope
		)

		try {
			dataSource.start()
			dataSource.start()

			assertEquals(listOf(true), calls)
		} finally {
			scope.cancel()
		}
	}

	@Test
	fun noOpUsageDataCollectionDataSource_startDoesNothing() {
		NoOpUsageDataCollectionDataSource.start()
	}

	@Test
	fun start_appliesConsentToEveryCollectionAction() = runTest {
		val firstCalls = mutableListOf<Boolean>()
		val secondCalls = mutableListOf<Boolean>()
		val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
		val repository = InMemoryUsageDataConsentRepository(initialValue = true)
		val dataSource = UsageDataCollectionDataSource(
			usageDataConsentRepository = repository,
			setCollectionEnabledActions = listOf(
				{ enabled -> firstCalls += enabled },
				{ enabled -> secondCalls += enabled }
			),
			coroutineScope = scope
		)

		try {
			dataSource.start()
			repository.setUsageDataCollectionEnabled(false)

			assertEquals(listOf(true, false), firstCalls)
			assertEquals(listOf(true, false), secondCalls)
		} finally {
			scope.cancel()
		}
	}
}
