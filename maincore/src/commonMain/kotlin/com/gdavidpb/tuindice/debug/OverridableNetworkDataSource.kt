package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import kotlin.concurrent.Volatile

/** Debug wrapper that lets a launch argument force the network availability. */
class OverridableNetworkDataSource(
	private val delegate: NetworkRepository
) : NetworkRepository {
	@Volatile
	var forced: Boolean? = null

	override fun isAvailable(): Boolean = forced ?: delegate.isAvailable()
}
