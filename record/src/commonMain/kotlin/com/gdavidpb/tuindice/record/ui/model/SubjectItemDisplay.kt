package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

data class SubjectItemDisplay(
	val status: SubjectStatus?,
	val gradeText: String
)

fun SubjectItem.toDisplay(currentGrade: Int): SubjectItemDisplay {
	val explicitStatus = status
		?.takeUnless { itemStatus -> itemStatus == SubjectStatus.NORMAL }
	val displayStatus = explicitStatus
		?: if (currentGrade == MIN_SUBJECT_GRADE) SubjectStatus.RETIRED else null
	val displayGradeText = if (
		(currentGrade == grade) &&
		(currentGrade != MIN_SUBJECT_GRADE)
	) {
		gradeText
	} else {
		"$currentGrade / $MAX_SUBJECT_GRADE"
	}

	return SubjectItemDisplay(
		status = displayStatus,
		gradeText = displayGradeText
	)
}
