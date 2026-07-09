package com.gdavidpb.tuindice.record.ui.model

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

enum class AttemptItemBadge {
	UNREPORTED,
	APPROVED,
	FAILED,
	RETIRED,
	WITHOUT_EFFECT,
	EQUIVALENCE
}

data class AttemptItemDisplay(
	val badge: AttemptItemBadge?,
	val gradeText: String
)

fun AttemptItem.toAttemptItemDisplay(currentGrade: Int): AttemptItemDisplay {
	val isQualitative = gradingMode == GradingMode.QUALITATIVE_PASS_FAIL
	val displayBadge = when {
		badge == AttemptBadge.WITHOUT_EFFECT -> AttemptItemBadge.WITHOUT_EFFECT
		badge == AttemptBadge.EQUIVALENCE -> AttemptItemBadge.EQUIVALENCE
		outcome == AttemptOutcome.UNREPORTED -> AttemptItemBadge.UNREPORTED
		isQualitative && (outcome == AttemptOutcome.APPROVED) -> AttemptItemBadge.APPROVED
		isQualitative && (outcome == AttemptOutcome.FAILED) -> AttemptItemBadge.FAILED
		outcome == AttemptOutcome.RETIRED -> AttemptItemBadge.RETIRED
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
