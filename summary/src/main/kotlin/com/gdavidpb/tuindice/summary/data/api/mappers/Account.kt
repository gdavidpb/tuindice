package com.gdavidpb.tuindice.summary.data.api.mappers

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.data.api.responses.AccountResponse

fun AccountResponse.toAccount() = Account(
	id = id,
	usbId = usbId,
	email = email,
	pictureUrl = pictureUrl,
	fullName = fullName,
	firstNames = firstNames,
	lastNames = lastNames,
	careerName = careerName,
	careerCode = careerCode,
	scholarship = scholarship,
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