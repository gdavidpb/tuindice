package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.IosRemoteConfigSubscription
import com.gdavidpb.tuindice.platform.IosRemoteConfigUpdateListener
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosRemoteConfigDataSourceTest {
	@Test
	fun observeUpdatedKeys_emitsTheKeysReportedByTheListener() = runTest {
		val capability = FakeIosRemoteConfigCapability()
		val dataSource = IosRemoteConfigDataSource(capability)
		val received = mutableListOf<Set<String>>()

		val collection = launch {
			dataSource.observeUpdatedKeys().collect { keys ->
				received.add(keys)
			}
		}
		runCurrent()

		capability.emit(setOf(RemoteConfigKeys.ASSISTANT_MODE))
		runCurrent()

		assertEquals(expected = listOf(setOf(RemoteConfigKeys.ASSISTANT_MODE)), actual = received)
		collection.cancelAndJoin()
	}

	@Test
	fun observeUpdatedKeys_cancellingTheCollectionCancelsTheSubscription() = runTest {
		val capability = FakeIosRemoteConfigCapability()
		val dataSource = IosRemoteConfigDataSource(capability)

		val collection = launch {
			dataSource.observeUpdatedKeys().collect { }
		}
		runCurrent()
		assertFalse(actual = capability.subscription.cancelled)

		collection.cancelAndJoin()

		assertTrue(actual = capability.subscription.cancelled)
	}

	private class FakeIosRemoteConfigCapability : IosRemoteConfigCapability {
		private var listener: IosRemoteConfigUpdateListener? = null
		val subscription = FakeIosRemoteConfigSubscription()

		override fun remoteConfigString(key: String): String? = null

		override fun addRemoteConfigUpdateListener(
			listener: IosRemoteConfigUpdateListener
		): IosRemoteConfigSubscription {
			this.listener = listener
			return subscription
		}

		fun emit(updatedKeys: Set<String>) {
			checkNotNull(listener) { "No listener is registered." }.onUpdate(updatedKeys)
		}
	}

	private class FakeIosRemoteConfigSubscription : IosRemoteConfigSubscription {
		var cancelled = false
			private set

		override fun cancel() {
			cancelled = true
		}
	}
}
