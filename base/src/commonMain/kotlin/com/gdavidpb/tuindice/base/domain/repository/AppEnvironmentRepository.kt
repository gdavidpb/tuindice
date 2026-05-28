package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment

interface AppEnvironmentRepository {
	fun getEnvironment(): AppEnvironment
}
