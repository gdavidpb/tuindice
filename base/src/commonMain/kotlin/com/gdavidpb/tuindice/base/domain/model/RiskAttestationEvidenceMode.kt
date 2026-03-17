package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RiskAttestationEvidenceMode(val value: String) {
	@SerialName("play_integrity")
	PLAY_INTEGRITY("play_integrity"),

	@SerialName("app_attest_attestation")
	APP_ATTEST_ATTESTATION("app_attest_attestation"),

	@SerialName("app_attest_assertion")
	APP_ATTEST_ASSERTION("app_attest_assertion")
}
