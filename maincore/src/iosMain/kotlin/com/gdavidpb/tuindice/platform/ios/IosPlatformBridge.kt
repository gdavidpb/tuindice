package com.gdavidpb.tuindice.platform.ios

import com.gdavidpb.tuindice.domain.model.IosHostCapabilities

interface IosPlatformBridge :
	IosRemoteConfigCapability,
	IosAttestationCapability,
	IosPushCapability,
	IosReviewCapability,
	IosUpdateCapability,
	IosExternalActionsCapability,
	IosDeviceCapability,
	IosObservabilityCapability

fun IosPlatformBridge.toHostCapabilities(): IosHostCapabilities {
	return IosHostCapabilities(
		remoteConfig = this,
		attestation = this,
		push = this,
		review = this,
		update = this,
		externalActions = this,
		device = this,
		observability = this
	)
}
