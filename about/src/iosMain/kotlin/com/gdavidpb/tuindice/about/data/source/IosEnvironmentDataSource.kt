package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.source.EnvironmentDataSource
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
class IosEnvironmentDataSource : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean {
		return Platform.isDebugBinary
	}
}
