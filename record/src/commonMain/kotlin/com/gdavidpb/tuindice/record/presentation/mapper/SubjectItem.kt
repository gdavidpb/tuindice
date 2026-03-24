package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

@Composable
fun Subject.toSubjectItem(
	isReadOnly: Boolean,
	texts: RecordMapperTexts
) = SubjectItem(
	subjectId = id,
	quarterId = quarterId,
	grade = grade,
	codeAndStatusText = buildAnnotatedString {
		withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
			append(code)
		}

		when {
			(grade == MIN_SUBJECT_GRADE) -> texts.subjectRetired
			else -> null
		}?.let { status ->
			append(" ")

			withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
				append(texts.subjectStatus(status))
			}
		}
	},
	nameText = name,
	gradeText = if (grade != MIN_SUBJECT_GRADE)
		texts.subjectGrade(grade)
	else
		"—",
	creditsText = texts.subjectCredits(credits),
	isRetired = (grade == MIN_SUBJECT_GRADE),
	isReadOnly = isReadOnly
)
