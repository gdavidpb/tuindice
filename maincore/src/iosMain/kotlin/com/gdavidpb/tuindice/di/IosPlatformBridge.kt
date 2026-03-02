package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction

data class IosPlatformAttestation(
	val token: String,
	val keyId: String? = null,
	val provider: AttestationProvider = AttestationProvider.APP_ATTEST
)

interface IosRemoteConfigCapability {
	suspend fun fetchRemoteConfig() {}

	fun remoteConfigString(key: String): String?
}

interface IosAttestationCapability {
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
	fun openFile(fileRef: PlatformFileRef): Boolean
	fun canOpen(fileRef: PlatformFileRef): Boolean
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

interface IosSecureStoreCapability {
	fun secureStoreContains(key: String): Boolean
	fun secureStoreGetString(key: String): String?
	fun secureStorePutString(key: String, value: String)
	fun secureStoreClear()
}

interface IosPlatformBridge :
	IosRemoteConfigCapability,
	IosAttestationCapability,
	IosPushCapability,
	IosReviewCapability,
	IosUpdateCapability,
	IosExternalActionsCapability,
	IosDeviceCapability,
	IosObservabilityCapability,
	IosSecureStoreCapability

internal data class IosHostCapabilities(
	val remoteConfig: IosRemoteConfigCapability,
	val attestation: IosAttestationCapability,
	val push: IosPushCapability,
	val review: IosReviewCapability,
	val update: IosUpdateCapability,
	val externalActions: IosExternalActionsCapability,
	val device: IosDeviceCapability,
	val observability: IosObservabilityCapability,
	val secureStore: IosSecureStoreCapability
)

internal fun IosPlatformBridge.toHostCapabilities(): IosHostCapabilities {
	return IosHostCapabilities(
		remoteConfig = this,
		attestation = this,
		push = this,
		review = this,
		update = this,
		externalActions = this,
		device = this,
		observability = this,
		secureStore = this
	)
}
