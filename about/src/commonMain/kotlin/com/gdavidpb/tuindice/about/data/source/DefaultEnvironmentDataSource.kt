package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository

class DefaultEnvironmentDataSource(
	private val appEnvironmentRepository: AppEnvironmentRepository
) : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean {
		return appEnvironmentRepository.getEnvironment().debug
	}
}
