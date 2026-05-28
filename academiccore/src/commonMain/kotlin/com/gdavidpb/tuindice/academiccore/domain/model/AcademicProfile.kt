package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicProfile(
	@SerialName("user_id") val userId: String = "",
	@SerialName("identity_card_number") val identityCardNumber: Int = 0,
	@SerialName("usb_id") val usbId: String = "",
	val email: String = "",
	@SerialName("first_names") val firstNames: String = "",
	@SerialName("last_names") val lastNames: String = "",
	@SerialName("career_name") val careerName: String = "",
	@SerialName("career_code") val careerCode: Int = 0,
	val scholarship: Boolean = false
)
