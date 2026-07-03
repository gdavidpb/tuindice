package com.gdavidpb.tuindice.security.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttestationEvidenceMode(val value: String) {
	@SerialName("play_integrity_standard")
	PLAY_INTEGRITY_STANDARD("play_integrity_standard"),

	@SerialName("play_integrity_classic")
	PLAY_INTEGRITY_CLASSIC("play_integrity_classic"),

	@SerialName("app_attest_attestation")
	APP_ATTEST_ATTESTATION("app_attest_attestation"),

	@SerialName("app_attest_assertion")
	APP_ATTEST_ASSERTION("app_attest_assertion")
}
