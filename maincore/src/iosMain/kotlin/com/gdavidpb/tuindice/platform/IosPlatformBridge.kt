package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.domain.model.IosHostCapabilities

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

fun IosPlatformBridge.toHostCapabilities(): IosHostCapabilities {
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
