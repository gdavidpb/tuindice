package com.gdavidpb.tuindice.base.domain.usecase.base

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InitialContentLoadTest {
	@Test
	fun missingOnly_whenLocalContentExists_emitsCachedAndSkipsRefresh() = runTest {
		val refreshRequests = mutableListOf<InitialContentRefreshRequest>()

		val results = ensureInitialContentLoaded(
			hasLocalContent = { true },
			refreshPolicy = InitialContentRefreshPolicy.MissingOnly,
			refresh = { request -> refreshRequests += request }
		).toList()

		assertEquals(listOf(InitialContentLoadResult.Cached), results)
		assertEquals(emptyList(), refreshRequests)
	}

	@Test
	fun missingOnly_whenLocalContentIsMissing_refreshesForcingRemote() = runTest {
		val refreshRequests = mutableListOf<InitialContentRefreshRequest>()

		val results = ensureInitialContentLoaded(
			hasLocalContent = { false },
			refreshPolicy = InitialContentRefreshPolicy.MissingOnly,
			refresh = { request -> refreshRequests += request }
		).toList()

		assertEquals(
			listOf(
				InitialContentLoadResult.RefreshStarted,
				InitialContentLoadResult.RefreshSucceeded
			),
			results
		)
		assertEquals(
			listOf(InitialContentRefreshRequest(InitialContentFreshness.ForceRemote)),
			refreshRequests
		)
	}

	@Test
	fun always_whenLocalContentExists_emitsCachedAndRefreshesRespectingCooldown() = runTest {
		val refreshRequests = mutableListOf<InitialContentRefreshRequest>()

		val results = ensureInitialContentLoaded(
			hasLocalContent = { true },
			refreshPolicy = InitialContentRefreshPolicy.Always,
			refresh = { request -> refreshRequests += request }
		).toList()

		assertEquals(
			listOf(
				InitialContentLoadResult.Cached,
				InitialContentLoadResult.RefreshStarted,
				InitialContentLoadResult.RefreshSucceeded
			),
			results
		)
		assertEquals(
			listOf(InitialContentRefreshRequest(InitialContentFreshness.RespectCooldown)),
			refreshRequests
		)
	}

	@Test
	fun refreshFailure_isPropagatedAfterRefreshStarted() = runTest {
		val throwable = IllegalStateException("refresh failed")
		val emitted = mutableListOf<InitialContentLoadResult>()

		assertFailsWith<IllegalStateException> {
			ensureInitialContentLoaded(
				hasLocalContent = { false },
				refreshPolicy = InitialContentRefreshPolicy.Always,
				refresh = { _ -> throw throwable }
			).toList(emitted)
		}

		assertEquals(listOf<InitialContentLoadResult>(InitialContentLoadResult.RefreshStarted), emitted)
	}
}
