package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.style.SubjectColorGenerator

@Composable
fun Subject.toSubjectItem(
	isReadOnly: Boolean,
	texts: RecordMapperTexts
) = SubjectColorGenerator.fromCode(code).let { subjectColors ->
	SubjectItem(
		subjectId = id,
		quarterId = quarterId,
		grade = grade,
		codeText = code,
		nameText = name,
		gradeText = if (grade != MIN_SUBJECT_GRADE)
			texts.subjectGrade(grade)
		else
			"",
		creditsText = texts.subjectCredits(credits),
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		isRetired = (grade == MIN_SUBJECT_GRADE),
		isReadOnly = isReadOnly
	)
}
