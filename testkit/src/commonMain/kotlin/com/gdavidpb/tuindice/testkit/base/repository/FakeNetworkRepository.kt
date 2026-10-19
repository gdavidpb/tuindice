package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository

class FakeNetworkRepository(
	private val isAvailable: Boolean = true
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
}
