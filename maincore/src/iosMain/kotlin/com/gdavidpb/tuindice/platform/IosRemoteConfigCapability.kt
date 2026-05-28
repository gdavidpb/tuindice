package com.gdavidpb.tuindice.platform

interface IosRemoteConfigCapability {
	suspend fun fetchRemoteConfig() {}

	fun remoteConfigString(key: String): String?
}
