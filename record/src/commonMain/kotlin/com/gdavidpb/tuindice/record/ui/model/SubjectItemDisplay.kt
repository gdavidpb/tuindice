package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

enum class SubjectItemBadge {
	RETIRED,
	WITHOUT_EFFECT
}

data class SubjectItemDisplay(
	val badge: SubjectItemBadge?,
	val gradeText: String
)

fun SubjectItem.toDisplay(currentGrade: Int): SubjectItemDisplay {
	val explicitStatus = status
		?.takeUnless { itemStatus -> itemStatus == SubjectStatus.NORMAL }
	val displayBadge = when {
		explicitStatus == SubjectStatus.RETIRED -> SubjectItemBadge.RETIRED
		explicitStatus == SubjectStatus.WITHOUT_EFFECT -> SubjectItemBadge.WITHOUT_EFFECT
		currentGrade == MIN_SUBJECT_GRADE -> SubjectItemBadge.RETIRED
		else -> null
	}
	val displayGradeText = if (
		(currentGrade == grade) &&
		(currentGrade != MIN_SUBJECT_GRADE)
	) {
		gradeText
	} else {
		"$currentGrade / $MAX_SUBJECT_GRADE"
	}

	return SubjectItemDisplay(
		badge = displayBadge,
		gradeText = displayGradeText
	)
}
