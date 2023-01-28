package com.gdavidpb.tuindice.summary.data.api.response

import com.google.gson.annotations.SerializedName

data class AccountResponse(
	@SerializedName("id")
	val id: String,
	@SerializedName("c_id")
	val cid: String,
	@SerializedName("usb_id")
	val usbId: String,
	@SerializedName("email")
	val email: String,
	@SerializedName("full_name")
	val fullName: String,
	@SerializedName("first_names")
	val firstNames: String,
	@SerializedName("last_names")
	val lastNames: String,
	@SerializedName("picture_url")
	val pictureUrl: String,
	@SerializedName("career_name")
	val careerName: String,
	@SerializedName("career_code")
	val careerCode: Int,
	@SerializedName("scholarship")
	val scholarship: Boolean,
	@SerializedName("grade")
	val grade: Double,
	@SerializedName("enrolled_subjects")
	val enrolledSubjects: Int,
	@SerializedName("enrolled_credits")
	val enrolledCredits: Int,
	@SerializedName("approved_subjects")
	val approvedSubjects: Int,
	@SerializedName("approved_credits")
	val approvedCredits: Int,
	@SerializedName("retired_subjects")
	val retiredSubjects: Int,
	@SerializedName("retired_credits")
	val retiredCredits: Int,
	@SerializedName("failed_subjects")
	val failedSubjects: Int,
	@SerializedName("failed_credits")
	val failedCredits: Int,
	@SerializedName("last_update")
	val lastUpdate: Long
)
