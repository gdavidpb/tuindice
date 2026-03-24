package com.gdavidpb.tuindice.record.testing

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

fun recordContentState(
	quarters: List<Quarter> = listOf(DEFAULT_RECORD_QUARTER)
): Record.State.Content = Record.State.Content(quarters = quarters)

fun recordMapperTexts(): RecordMapperTexts = RecordMapperTexts(
	quarterGradeDiff = { grade -> "Δx $grade" },
	quarterGradeSum = { grade -> "∑x $grade" },
	quarterCredits = { credits -> "⦿ $credits UC" },
	subjectRetired = "Retirada",
	subjectStatus = { status -> "($status)" },
	subjectGrade = { grade -> "$grade / 5" },
	subjectCredits = { credits -> "$credits UC" }
)

fun sampleSubjectItem(
	subjectId: String = "subject-1",
	quarterId: String = "quarter-1",
	grade: Int = 4,
	isReadOnly: Boolean = true
): SubjectItem = SubjectItem(
	subjectId = subjectId,
	quarterId = quarterId,
	grade = grade,
	codeAndStatusText = AnnotatedString("FS1113"),
	nameText = "FISICA III",
	gradeText = "$grade / 5",
	creditsText = "3 UC",
	isReadOnly = isReadOnly,
	isRetired = false
)

fun sampleQuarterItem(
	quarterId: String = "quarter-1",
	canDelete: Boolean = false,
	subjects: List<SubjectItem> = listOf(sampleSubjectItem())
): QuarterItem = QuarterItem(
	quarterId = quarterId,
	nameText = "Abril - Julio 2023",
	gradeText = AnnotatedString("Δx 4.2500"),
	gradeSumText = AnnotatedString("∑x 4.2500"),
	creditsText = AnnotatedString("⦿ 6 UC"),
	canDelete = canDelete,
	subjects = subjects,
	states = hashMapOf(
		subjects.first().subjectId to mutableIntStateOf(subjects.first().grade)
	)
)
