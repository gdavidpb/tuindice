package com.gdavidpb.tuindice.summary.data.repository.account.source.api.mapper

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.data.repository.account.source.api.response.AccountResponse
import java.util.*

fun AccountResponse.toAccount() = Account(
	id = id,
	cid = cid,
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