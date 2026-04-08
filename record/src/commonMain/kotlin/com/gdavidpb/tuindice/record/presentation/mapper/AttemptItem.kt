package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScoreKind
import com.gdavidpb.tuindice.academiccore.domain.model.HistoricalBadge
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

@Composable
fun AttemptProjection.toAttemptItem(
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
			(score.kind != AttemptScoreKind.EMPTY || resolvedStatus == SubjectStatus.UNREPORTED)
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
	if (badge == HistoricalBadge.WITHOUT_EFFECT) {
		return SubjectStatus.WITHOUT_EFFECT
	}

	return when (outcome) {
		OfficialOutcome.PENDING -> null
		OfficialOutcome.APPROVED -> SubjectStatus.APPROVED
		OfficialOutcome.FAILED -> SubjectStatus.FAILED
		OfficialOutcome.RETIRED -> SubjectStatus.RETIRED
		OfficialOutcome.UNREPORTED -> SubjectStatus.UNREPORTED
	}
}
