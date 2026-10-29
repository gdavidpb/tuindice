package com.gdavidpb.tuindice.platform

interface IosRemoteConfigCapability {
	suspend fun fetchRemoteConfig() {}

	fun remoteConfigString(key: String): String?

	fun addRemoteConfigUpdateListener(listener: IosRemoteConfigUpdateListener): IosRemoteConfigSubscription
}
