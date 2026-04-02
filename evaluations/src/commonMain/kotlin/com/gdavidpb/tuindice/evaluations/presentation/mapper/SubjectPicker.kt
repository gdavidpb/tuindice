package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationSubjectPickerItem

fun List<Subject>.toEvaluationSubjectPickerItemList(
	selectedSubject: Subject?
): List<EvaluationSubjectPickerItem> {
	return map { subject ->
		SubjectColorGenerator.fromCode(subject.code).let { subjectColors ->
			EvaluationSubjectPickerItem(
				subject = subject,
				labelText = subject.code,
				isSelected = (subject == selectedSubject),
				isVisible = (selectedSubject == null) || (subject == selectedSubject),
				containerColor = subjectColors.containerColor,
				contentColor = subjectColors.color,
				disabledContainerColor = subjectColors.containerColor.copy(alpha = 0.55f),
				disabledContentColor = subjectColors.color.copy(alpha = 0.38f)
			)
		}
	}
}

fun List<EvaluationSubjectPickerItem>.withSelectedSubject(
	selectedSubject: Subject?
): List<EvaluationSubjectPickerItem> {
	return map { item ->
		item.copy(
			isSelected = (item.subject == selectedSubject),
			isVisible = (selectedSubject == null) || (item.subject == selectedSubject)
		)
	}
}
