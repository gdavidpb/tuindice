package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

@Composable
fun AttemptProjection.toAttemptItem(
	termId: String,
	isReadOnly: Boolean,
	texts: RecordMapperTexts
): AttemptItem = remember(subjectCode) { SubjectColorGenerator.fromCode(subjectCode) }.let { subjectColors ->
	val resolvedStatus = toUiStatus()
	val numericGrade = score.numericValue ?: MIN_SUBJECT_GRADE

	AttemptItem(
		attemptId = id,
		termId = termId,
		grade = numericGrade,
		gradingMode = when (gradingMode) {
			AttemptGradingMode.NUMERIC -> GradingMode.NUMERIC
			AttemptGradingMode.QUALITATIVE_PASS_FAIL -> GradingMode.QUALITATIVE_PASS_FAIL
		},
		status = resolvedStatus,
		codeText = subjectCode,
		nameText = subjectName,
		gradeText = if (
			(this.gradingMode == AttemptGradingMode.NUMERIC) &&
			(
				(numericGrade != MIN_SUBJECT_GRADE) ||
					(resolvedStatus == SubjectStatus.UNREPORTED)
				) &&
			(resolvedStatus != SubjectStatus.RETIRED) &&
			(score !is AttemptScore.Empty || resolvedStatus == SubjectStatus.UNREPORTED)
		)
			texts.termAttemptGrade(numericGrade)
		else
			"",
		creditsText = texts.termAttemptCredits(credits),
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		isReadOnly = isReadOnly
	)
}

private fun AttemptProjection.toUiStatus(): SubjectStatus? {
	if (badge == AttemptBadge.WITHOUT_EFFECT) {
		return SubjectStatus.WITHOUT_EFFECT
	}

	return when (outcome) {
		AttemptOutcome.PENDING -> null
		AttemptOutcome.APPROVED -> SubjectStatus.APPROVED
		AttemptOutcome.FAILED -> SubjectStatus.FAILED
		AttemptOutcome.RETIRED -> SubjectStatus.RETIRED
		AttemptOutcome.UNREPORTED -> SubjectStatus.UNREPORTED
	}
}
