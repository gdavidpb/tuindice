package com.gdavidpb.tuindice.data.source.environment

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.platform.IOSContext

class IosAppEnvironmentDataSource(
	private val iOSContext: IOSContext
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = iOSContext.appEnvironment
}
