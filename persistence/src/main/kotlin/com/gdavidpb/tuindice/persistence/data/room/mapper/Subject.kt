package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.base.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE

fun Subject.toSubjectEntity(uid: String) = SubjectEntity(
	id = id,
	quarterId = qid,
	accountId = uid,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status
)

fun SubjectEntity.toSubject(isEditable: Boolean) = Subject(
	id = id,
	qid = quarterId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	status = status,
	isEditable = isEditable,
	isRetired = (!isEditable && status == STATUS_SUBJECT_RETIRED) || (isEditable && grade == MIN_SUBJECT_GRADE),
	isNoEffect = (status == STATUS_SUBJECT_NO_EFFECT)
)