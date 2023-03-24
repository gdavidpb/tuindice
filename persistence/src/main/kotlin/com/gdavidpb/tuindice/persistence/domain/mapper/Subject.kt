package com.gdavidpb.tuindice.persistence.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateResolution
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity

fun Resolution.isSubject() = when (type) {
	ResolutionType.SUBJECT -> true
	else -> false
}

fun Resolution.toSubjectEntity() = (operation as SubjectUpdateResolution).run {
	SubjectEntity(
		id = id,
		quarterId = quarterId,
		accountId = uid,
		code = code,
		name = name,
		credits = credits,
		grade = grade,
		status = status
	)
}