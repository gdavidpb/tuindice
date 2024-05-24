package com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSubject

fun SubjectEntity.toLocalSubject(isEditable: Boolean) = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = (!isEditable && status == STATUS_SUBJECT_RETIRED) || (isEditable && grade == MIN_SUBJECT_GRADE),
	isNoEffect = (status == STATUS_SUBJECT_NO_EFFECT)
)

fun LocalSubject.toSubjectEntity(uid: String) = SubjectEntity(
	id = id,
	quarterId = quarterId,
	accountId = uid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun RemoteSubject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = isRetired,
	isNoEffect = isNoEffect
)

fun LocalSubject.toSubject() = Subject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = isRetired,
	isNoEffect = isNoEffect
)

fun Subject.toLocalSubject() = LocalSubject(
	id = id,
	quarterId = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = isRetired,
	isNoEffect = isNoEffect
)