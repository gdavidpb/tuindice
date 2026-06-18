package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.platform.IosDeviceCapability
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.IosPushCapability
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.IosReviewCapability
import com.gdavidpb.tuindice.platform.IosSecureStoreCapability
import com.gdavidpb.tuindice.platform.IosUpdateCapability

data class IosHostCapabilities(
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
