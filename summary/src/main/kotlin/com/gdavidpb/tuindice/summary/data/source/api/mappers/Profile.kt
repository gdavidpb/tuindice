package com.gdavidpb.tuindice.summary.data.source.api.mappers

import com.gdavidpb.tuindice.summary.data.source.api.responses.ProfileResponse
import com.gdavidpb.tuindice.summary.domain.model.Profile

fun ProfileResponse.toProfile() = Profile(
	fullName = fullName,
	firstNames = firstNames,
	lastNames = lastNames,
	profilePictureUrl = profilePictureUrl,
	careerName = careerName,
	grade = grade,
	enrolledSubjects = enrolledSubjects,
	enrolledCredits = enrolledCredits,
	approvedSubjects = approvedSubjects,
	approvedCredits = approvedCredits,
	retiredSubjects = retiredSubjects,
	retiredCredits = retiredCredits,
	failedSubjects = failedSubjects,
	failedCredits = failedCredits,
	lastUpdate = lastUpdate
)