package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// What DST says about an annulled enrollment. Both fields are free strings, never an enum: the
// code can change from one day to the next and a new one must not break anything.
@Serializable
data class EnrollmentSituation(
	@SerialName("code") val code: String? = null,
	@SerialName("description") val description: String? = null
) {
	val annulmentCause: EnrollmentAnnulmentCause
		get() = EnrollmentAnnulmentCause.fromCode(code)
}
