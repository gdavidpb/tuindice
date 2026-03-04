package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.platform.ios.IosAttestationCapability
import com.gdavidpb.tuindice.platform.ios.IosDeviceCapability
import com.gdavidpb.tuindice.platform.ios.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.ios.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.ios.IosPushCapability
import com.gdavidpb.tuindice.platform.ios.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.ios.IosReviewCapability
import com.gdavidpb.tuindice.platform.ios.IosUpdateCapability

data class IosHostCapabilities(
	val remoteConfig: IosRemoteConfigCapability,
	val attestation: IosAttestationCapability,
	val push: IosPushCapability,
	val review: IosReviewCapability,
	val update: IosUpdateCapability,
	val externalActions: IosExternalActionsCapability,
	val device: IosDeviceCapability,
	val observability: IosObservabilityCapability
)
