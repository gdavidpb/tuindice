package com.gdavidpb.tuindice.summary.data.mapper

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.data.room.entity.UserEntity

fun User.toUserEntity() = UserEntity(
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

fun UserEntity.toUser() = User(
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
