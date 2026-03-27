package com.gdavidpb.tuindice.record.testing

import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.style.SubjectColorGenerator

fun recordContentState(
	quarters: List<Quarter> = listOf(DEFAULT_RECORD_QUARTER)
): Record.State.Content = Record.State.Content(quarters = quarters)

fun recordMapperTexts(): RecordMapperTexts = RecordMapperTexts(
	quarterGradeDiff = { grade -> "Δx $grade" },
	quarterGradeSum = { grade -> "∑x $grade" },
	quarterCredits = { credits -> "⦿ $credits" },
	subjectGrade = { grade -> "$grade / 5" },
	subjectCredits = { credits -> "$credits UC" }
)

fun sampleSubjectItem(
	subjectId: String = "subject-1",
	quarterId: String = "quarter-1",
	grade: Int = 4,
	status: SubjectStatus? = null,
	isReadOnly: Boolean = true
): SubjectItem = SubjectColorGenerator.fromCode("FS1113").let { subjectColors ->
	SubjectItem(
		subjectId = subjectId,
		quarterId = quarterId,
		grade = grade,
		status = status,
		codeText = "FS1113",
		nameText = "FISICA III",
		gradeText = if ((grade == MIN_SUBJECT_GRADE) || (status != null)) "" else "$grade / 5",
		creditsText = "3 UC",
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		isReadOnly = isReadOnly
	)
}

fun sampleQuarterItem(
	quarterId: String = "quarter-1",
	shortNameText: String = "Abr. - Jul. 2023",
	gradeDelta: QuarterMetricDelta? = null,
	gradeSumDelta: QuarterMetricDelta? = null,
	isCurrent: Boolean = false,
	canDelete: Boolean = false,
	subjects: List<SubjectItem> = listOf(sampleSubjectItem())
): QuarterItem = QuarterItem(
	quarterId = quarterId,
	shortNameText = shortNameText,
	gradeText = AnnotatedString("Δx 4.2500"),
	gradeDelta = gradeDelta,
	gradeSumText = AnnotatedString("∑x 4.2500"),
	gradeSumDelta = gradeSumDelta,
	creditsText = AnnotatedString("⦿ 6"),
	isCurrent = isCurrent,
	canDelete = canDelete,
	subjects = subjects
)

fun sampleQuarterMetricDelta(
	text: String = "+0.50",
	tone: QuarterMetricDeltaTone = QuarterMetricDeltaTone.Positive
): QuarterMetricDelta = QuarterMetricDelta(
	text = text,
	tone = tone
)
