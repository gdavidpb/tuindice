package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttestationProofOfPossessionMode(val value: String) {
	@SerialName("android_keystore")
	ANDROID_KEYSTORE("android_keystore")
}
