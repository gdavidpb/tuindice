package com.gdavidpb.tuindice.persistence.domain.mapper

import com.gdavidpb.tuindice.persistence.data.api.response.SubjectDataResponse
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType

fun Resolution.isSubject() = when {
	data is SubjectDataResponse -> true
	type == TransactionType.SUBJECT -> true
	else -> false
}

fun Resolution.toSubjectEntity() = (data as SubjectDataResponse).run {
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