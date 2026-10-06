package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverridableNetworkDataSourceTest {
	@Test
	fun withoutOverride_followsTheDelegate() {
		assertTrue(OverridableNetworkDataSource(FixedNetwork(true)).isAvailable())
		assertFalse(OverridableNetworkDataSource(FixedNetwork(false)).isAvailable())
	}

	@Test
	fun forcedTrue_winsOverAnOfflineDelegate() {
		val source = OverridableNetworkDataSource(FixedNetwork(false)).apply { forced = true }

		assertTrue(source.isAvailable())
	}

	@Test
	fun forcedFalse_winsOverAnOnlineDelegate() {
		val source = OverridableNetworkDataSource(FixedNetwork(true)).apply { forced = false }

		assertFalse(source.isAvailable())
	}

	@Test
	fun clearingTheOverride_returnsToTheDelegate() {
		val source = OverridableNetworkDataSource(FixedNetwork(true)).apply { forced = false }

		source.forced = null

		assertTrue(source.isAvailable())
	}

	private class FixedNetwork(private val available: Boolean) : NetworkRepository {
		override fun isAvailable(): Boolean = available
	}
}
