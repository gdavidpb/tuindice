package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

@Composable
fun AttemptProjection.toAttemptItem(
	isReadOnly: Boolean,
	texts: RecordMapperTexts
): AttemptItem = remember(subjectCode) { CourseCodeColorGenerator.fromCode(subjectCode) }.let { subjectColors ->
	val resolvedOutcome = outcome.takeUnless { candidate -> candidate == AttemptOutcome.PENDING }
	val numericGrade = score.numericValue ?: MIN_SUBJECT_GRADE

	AttemptItem(
		attemptId = id,
		subjectCode = subjectCode,
		grade = numericGrade,
		gradingMode = when (gradingMode) {
			AttemptGradingMode.NUMERIC -> GradingMode.NUMERIC
			AttemptGradingMode.QUALITATIVE_PASS_FAIL -> GradingMode.QUALITATIVE_PASS_FAIL
		},
		outcome = resolvedOutcome,
		badge = badge,
		codeText = subjectCode,
		nameText = subjectName,
		gradeText = if (
			(this.gradingMode == AttemptGradingMode.NUMERIC) &&
			(
				(numericGrade != MIN_SUBJECT_GRADE) ||
					(resolvedOutcome == AttemptOutcome.UNREPORTED)
				) &&
			(resolvedOutcome != AttemptOutcome.RETIRED) &&
			(score !is AttemptScore.Empty || resolvedOutcome == AttemptOutcome.UNREPORTED)
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
