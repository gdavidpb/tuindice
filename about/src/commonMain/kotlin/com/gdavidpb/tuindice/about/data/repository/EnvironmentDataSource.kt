package com.gdavidpb.tuindice.about.data.repository

interface EnvironmentDataSource {
	fun isDebugEnvironment(): Boolean
}
