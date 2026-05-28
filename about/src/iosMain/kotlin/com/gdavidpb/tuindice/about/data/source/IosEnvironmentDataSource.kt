package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataRepository
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
class IosEnvironmentDataSource : EnvironmentDataRepository {
	override fun isDebugEnvironment(): Boolean {
		return Platform.isDebugBinary
	}
}
