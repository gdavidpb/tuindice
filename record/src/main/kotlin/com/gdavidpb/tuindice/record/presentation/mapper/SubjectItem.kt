package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

@Composable
fun Subject.toSubjectItem() = SubjectItem(
	subjectId = id,
	quarterId = qid,
	grade = grade,
	codeAndStatusText = buildAnnotatedString {
		withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
			append(code)
		}

		when {
			(grade == MIN_SUBJECT_GRADE) || isRetired -> stringResource(id = R.string.subject_retired)
			isNoEffect -> stringResource(id = R.string.subject_no_effect)
			else -> null
		}?.let { status ->
			append(" ")

			withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
				append(stringResource(id = R.string.subject_status, status))
			}
		}
	},
	nameText = name,
	gradeText = if (grade != MIN_SUBJECT_GRADE)
		stringResource(id = R.string.subject_grade, grade)
	else
		"—",
	creditsText = stringResource(id = R.string.subject_credits, credits),
	isRetired = (grade == MIN_SUBJECT_GRADE) || isRetired,
	isNoEffect = isNoEffect,
	isEditable = isEditable
)