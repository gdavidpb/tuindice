package com.gdavidpb.tuindice.base.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SyncedContentResolutionTest {
	@Test
	fun resolveSyncedContentResolution_prefersContent() {
		assertEquals(
			SyncedContentResolution.Content,
			resolveSyncedContentResolution(
				hasContent = true,
				hasSynced = false,
				keepCurrentWhileWaiting = true
			)
		)
	}

	@Test
	fun resolveSyncedContentResolution_returnsEmpty_afterSyncWithoutContent() {
		assertEquals(
			SyncedContentResolution.Empty,
			resolveSyncedContentResolution(
				hasContent = false,
				hasSynced = true,
				keepCurrentWhileWaiting = false
			)
		)
	}

	@Test
	fun resolveSyncedContentResolution_keepsCurrent_whileWaitingWhenRequested() {
		assertEquals(
			SyncedContentResolution.KeepCurrent,
			resolveSyncedContentResolution(
				hasContent = false,
				hasSynced = false,
				keepCurrentWhileWaiting = true
			)
		)
	}

	@Test
	fun resolveSyncedContentResolution_returnsLoading_whileWaitingWithoutCurrentState() {
		assertEquals(
			SyncedContentResolution.Loading,
			resolveSyncedContentResolution(
				hasContent = false,
				hasSynced = false,
				keepCurrentWhileWaiting = false
			)
		)
	}
}
