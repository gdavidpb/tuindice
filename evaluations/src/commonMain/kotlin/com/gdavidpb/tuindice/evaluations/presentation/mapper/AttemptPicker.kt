package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationAttemptPickerItem

fun List<EditableAttemptDescriptor>.toEvaluationAttemptPickerItems(
	selectedAttempt: EditableAttemptDescriptor?
): List<EvaluationAttemptPickerItem> {
	return filter { attempt -> attempt.gradingMode == GradingMode.NUMERIC }
		.map { attempt ->
			CourseCodeColorGenerator.fromCode(attempt.code).let { subjectColors ->
				EvaluationAttemptPickerItem(
					attempt = attempt,
					labelText = attempt.code,
					isSelected = (attempt == selectedAttempt),
					isVisible = (selectedAttempt == null) || (attempt == selectedAttempt),
					containerColor = subjectColors.containerColor,
					contentColor = subjectColors.color,
					disabledContainerColor = subjectColors.containerColor.copy(alpha = 0.55f),
					disabledContentColor = subjectColors.color.copy(alpha = 0.38f)
				)
			}
		}
}

fun List<EvaluationAttemptPickerItem>.withSelectedAttempt(
	selectedAttempt: EditableAttemptDescriptor?
): List<EvaluationAttemptPickerItem> {
	return map { item ->
		item.copy(
			isSelected = (item.attempt == selectedAttempt),
			isVisible = (selectedAttempt == null) || (item.attempt == selectedAttempt)
		)
	}
}
