package com.gdavidpb.tuindice.persistence.data.source.room.mappers

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.persistence.data.source.room.entities.AccountEntity

fun Account.toAccountEntity(uid: String) = AccountEntity(
	id = uid,
	cid = id,
	usbId = usbId,
	email = email,
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

fun AccountEntity.toAccount() = Account(
	id = id,
	usbId = usbId,
	email = email,
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