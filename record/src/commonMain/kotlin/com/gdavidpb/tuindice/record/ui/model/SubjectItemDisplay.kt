package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

enum class SubjectItemBadge {
	APPROVED,
	FAILED,
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
	val isQualitative = gradingMode == GradingMode.QUALITATIVE_PASS_FAIL
	val displayBadge = when {
		isQualitative && (explicitStatus == SubjectStatus.APPROVED) -> SubjectItemBadge.APPROVED
		isQualitative && (explicitStatus == SubjectStatus.FAILED) -> SubjectItemBadge.FAILED
		explicitStatus == SubjectStatus.RETIRED -> SubjectItemBadge.RETIRED
		explicitStatus == SubjectStatus.WITHOUT_EFFECT -> SubjectItemBadge.WITHOUT_EFFECT
		!isQualitative && (currentGrade == MIN_SUBJECT_GRADE) -> SubjectItemBadge.RETIRED
		else -> null
	}
	val displayGradeText = if (
		isQualitative
	) {
		""
	} else if (
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
