package com.gdavidpb.tuindice.data.source.environment

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository

class IosAppEnvironmentDataSource(
	private val environment: AppEnvironment
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = environment
}
