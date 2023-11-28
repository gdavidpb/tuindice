package com.gdavidpb.tuindice.summary.data.repository.account.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
	@SerialName("id") val id: String,
	@SerialName("c_id") val cid: String,
	@SerialName("usb_id") val usbId: String,
	@SerialName("email") val email: String,
	@SerialName("full_name") val fullName: String,
	@SerialName("first_names") val firstNames: String,
	@SerialName("last_names") val lastNames: String,
	@SerialName("picture_url") val pictureUrl: String,
	@SerialName("career_name") val careerName: String,
	@SerialName("career_code") val careerCode: Int,
	@SerialName("scholarship") val scholarship: Boolean,
	@SerialName("grade") val grade: Double,
	@SerialName("enrolled_subjects") val enrolledSubjects: Int,
	@SerialName("enrolled_credits") val enrolledCredits: Int,
	@SerialName("approved_subjects") val approvedSubjects: Int,
	@SerialName("approved_credits") val approvedCredits: Int,
	@SerialName("retired_subjects") val retiredSubjects: Int,
	@SerialName("retired_credits") val retiredCredits: Int,
	@SerialName("failed_subjects") val failedSubjects: Int,
	@SerialName("failed_credits") val failedCredits: Int,
	@SerialName("last_update") val lastUpdate: Long
)
