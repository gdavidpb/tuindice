package com.gdavidpb.tuindice.platform

interface IosObservabilityCapability {
	fun setUserIdentifier(identifier: String)
	fun setAnalyticsCollectionEnabled(enabled: Boolean)
	fun logEvent(name: String, parameters: Map<String, String>)
	fun logMessage(message: String)
	fun logException(throwable: Throwable)
	fun setCustomKey(key: String, value: String)
}
