package com.gdavidpb.tuindice.summary.domain.model

import java.util.*

data class Profile(
	val fullName: String,
	val firstNames: String,
	val lastNames: String,
	val profilePictureUrl: String,
	val careerName: String,
	val grade: Double,
	val enrolledSubjects: Int,
	val enrolledCredits: Int,
	val approvedSubjects: Int,
	val approvedCredits: Int,
	val retiredSubjects: Int,
	val retiredCredits: Int,
	val failedSubjects: Int,
	val failedCredits: Int,
	val lastUpdate: Date
)