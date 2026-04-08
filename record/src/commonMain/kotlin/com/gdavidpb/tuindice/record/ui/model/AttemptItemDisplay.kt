package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

enum class AttemptItemBadge {
	UNREPORTED,
	APPROVED,
	FAILED,
	RETIRED,
	WITHOUT_EFFECT
}

data class AttemptItemDisplay(
	val badge: AttemptItemBadge?,
	val gradeText: String
)

fun AttemptItem.toAttemptItemDisplay(currentGrade: Int): AttemptItemDisplay {
	val explicitStatus = status
		?.takeUnless { itemStatus -> itemStatus == SubjectStatus.NORMAL }
	val isQualitative = gradingMode == GradingMode.QUALITATIVE_PASS_FAIL
	val displayBadge = when {
		explicitStatus == SubjectStatus.UNREPORTED -> AttemptItemBadge.UNREPORTED
		isQualitative && (explicitStatus == SubjectStatus.APPROVED) -> AttemptItemBadge.APPROVED
		isQualitative && (explicitStatus == SubjectStatus.FAILED) -> AttemptItemBadge.FAILED
		explicitStatus == SubjectStatus.RETIRED -> AttemptItemBadge.RETIRED
		explicitStatus == SubjectStatus.WITHOUT_EFFECT -> AttemptItemBadge.WITHOUT_EFFECT
		!isQualitative && (currentGrade == MIN_SUBJECT_GRADE) -> AttemptItemBadge.RETIRED
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

	return AttemptItemDisplay(
		badge = displayBadge,
		gradeText = displayGradeText
	)
}
