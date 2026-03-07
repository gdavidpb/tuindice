package com.gdavidpb.tuindice.platform.ios

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation

interface IosRemoteConfigCapability {
	suspend fun fetchRemoteConfig() {}

	fun remoteConfigString(key: String): String?
}

interface IosAttestationCapability {
	fun sha256Base64Url(value: String): String?
	fun markAttestationKeyRegistered(keyId: String)
	suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation?
}

interface IosPushCapability {
	suspend fun pushToken(): String?
}

interface IosReviewCapability {
	suspend fun launchReview()
}

interface IosUpdateCapability {
	suspend fun checkForUpdate(stalenessDays: Int): UpdateAction?
	suspend fun launchUpdate(action: UpdateAction)
}

interface IosExternalActionsCapability {
	fun openUrl(url: String)
	fun openFile(path: String): Boolean
	fun canOpen(path: String): Boolean
}

interface IosDeviceCapability {
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
	fun isNetworkAvailable(): Boolean
}

interface IosObservabilityCapability {
	fun setUserIdentifier(identifier: String)
	fun logMessage(message: String)
	fun logException(throwable: Throwable)
	fun setCustomKey(key: String, value: String)
}
