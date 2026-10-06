package com.gdavidpb.tuindice.record.testing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind

// The colours a subject code is drawn in when a test builds its item by hand.
val FixtureCodeColor = Color(0xFF1E3A5F)
val FixtureCodeContainerColor = Color(0xFFDCE8F5)

/**
 * A term as the mapper hands it to the views: no deltas and no notice, with the flags a term of
 * that kind has. A test that needs anything else copies it.
 */
fun termItem(
	termId: String = "term-1",
	shortNameText: String = "Sep - Dic 2026",
	kind: TermItemKind = TermItemKind.HISTORICAL,
	attempts: List<AttemptItem> = emptyList()
) = TermItem(
	termId = termId,
	periodYear = 2026,
	termOrder = 20266,
	shortNameText = shortNameText,
	kind = kind,
	gradeText = AnnotatedString("Δx 4.0000"),
	gradeDelta = null,
	gradeSumText = AnnotatedString("∑x 3.5000"),
	gradeSumDelta = null,
	creditsText = AnnotatedString("⦿ 12"),
	creditsDelta = null,
	isCurrent = kind == TermItemKind.CURRENT,
	canDelete = kind == TermItemKind.SYNTHETIC,
	canEdit = kind == TermItemKind.SYNTHETIC,
	attempts = attempts
)

// A numeric subject graded 4 unless told otherwise; qualitative ones carry no grade text.
fun attemptItem(
	attemptId: String = "attempt-1",
	subjectCode: String = "MA2115",
	gradingMode: GradingMode = GradingMode.NUMERIC,
	isReadOnly: Boolean = false
) = AttemptItem(
	attemptId = attemptId,
	subjectCode = subjectCode,
	grade = if (gradingMode == GradingMode.NUMERIC) 4 else 0,
	gradingMode = gradingMode,
	codeText = subjectCode,
	nameText = "MATERIA $subjectCode",
	gradeText = if (gradingMode == GradingMode.NUMERIC) "4 / 5" else "",
	creditsText = "4 UC",
	codeColor = FixtureCodeColor,
	codeContainerColor = FixtureCodeContainerColor,
	isReadOnly = isReadOnly
)

// Built by the mapper, so the name a card shows is the one the app resolves (in capitals).
fun createTermSubjectItem(
	subjectCode: String = "MA1111",
	name: String = "Matemáticas I",
	credits: Int = 4,
	availability: SyntheticTermSubjectAvailability = SyntheticTermSubjectAvailability.AVAILABLE
): CreateTermSubjectItem = SyntheticTermSubject(
	subjectCode = subjectCode,
	name = name,
	credits = credits,
	availability = availability
).toCreateTermSubjectItem()

// Texts a mapper test can read back: each says which pattern produced it.
fun recordMapperTexts() = RecordMapperTexts(
	termGrade = { value -> "$value prom" },
	termGradeSum = { value -> "$value acum" },
	termCredits = { credits -> "$credits UC" },
	termAttemptGrade = { grade -> "Nota $grade" },
	termAttemptCredits = { credits -> "$credits UC" },
	termAttemptSection = { section -> "Sección $section" },
	termAttemptSectionClassroom = { section, classroom -> "Sección $section · $classroom" }
)

// A spinner draws nothing a test can read; what it tells a screen reader is that work is in progress.
fun isProgressIndicator(): SemanticsMatcher =
	SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
