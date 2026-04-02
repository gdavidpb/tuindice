package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

@Composable
fun Subject.toSubjectItem(
	isReadOnly: Boolean,
	resolvedStatus: SubjectStatus?,
	texts: RecordMapperTexts
) = remember(code) { SubjectColorGenerator.fromCode(code) }.let { subjectColors ->
	SubjectItem(
		subjectId = id,
		quarterId = quarterId,
		grade = grade,
		status = resolvedStatus,
		codeText = code,
		nameText = name,
		gradeText = if ((grade != MIN_SUBJECT_GRADE) && (resolvedStatus != SubjectStatus.RETIRED))
			texts.subjectGrade(grade)
		else
			"",
		creditsText = texts.subjectCredits(credits),
		codeColor = subjectColors.color,
		codeContainerColor = subjectColors.containerColor,
		isReadOnly = isReadOnly
	)
}
