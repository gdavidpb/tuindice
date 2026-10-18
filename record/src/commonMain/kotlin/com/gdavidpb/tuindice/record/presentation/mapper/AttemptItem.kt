package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem

@Composable
fun AttemptProjection.toAttemptItem(
	isReadOnly: Boolean,
	texts: RecordMapperTexts,
	termLabel: String? = null
): AttemptItem = remember(subjectCode) { CourseCodeColorGenerator.fromCode(subjectCode) }.let { subjectColors ->
	val resolvedOutcome = outcome.takeUnless { candidate -> candidate == AttemptOutcome.PENDING }
	val numericGrade = score.numericValue ?: MIN_SUBJECT_GRADE
	val gradeText = if (
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
		""

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
		nameText = subjectName.uppercase(),
		gradeText = gradeText,
		creditsText = texts.termAttemptCredits(credits),
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		isReadOnly = isReadOnly || withdrawn,
		detailText = toEnrollmentDetailText(texts),
		enrollmentErrorText = toEnrollmentErrorText(),
		isWithdrawn = withdrawn,
		assistantContext = toAssistantContext(
			termLabel = termLabel,
			gradeText = gradeText,
			outcome = resolvedOutcome
		)
	)
}

// What the assistant reads of a subject asked about from its card: the attempt and its subject, and what the card
// shows of it as plain text, with the term it belongs to.
private fun AttemptProjection.toAssistantContext(
	termLabel: String?,
	gradeText: String,
	outcome: AttemptOutcome?
) = AssistantScreenContext(
	screen = AssistantScreen.RECORD,
	entities = listOf(
		AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = id),
		AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = subjectCode)
	),
	visibleFields = buildMap {
		put("subjectCode", subjectCode)
		put("subjectName", subjectName)
		if (termLabel != null) put("termLabel", termLabel)
		if (gradeText.isNotBlank()) put("grade", gradeText)
		if (outcome != null) put("outcome", outcome.name)
		put("credits", credits.toString())
		put("isWithdrawn", withdrawn.toString())
	},
	filters = emptyMap()
)
