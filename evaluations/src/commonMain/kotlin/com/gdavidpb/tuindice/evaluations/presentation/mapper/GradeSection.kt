package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade

@Composable
fun rememberEvaluationGradeSectionItem(
	isOverdue: Boolean,
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	val maxGradeTitleText = stringResource(Res.string.label_add_evaluation_max_grade)
	val gradesTitleText = stringResource(Res.string.label_add_evaluation_grades)

	return remember(
		isOverdue,
		grade,
		maxGrade,
		maxGradeTitleText,
		gradesTitleText
	) {
		EvaluationGradeSectionItem(
			titleText = if (isOverdue) gradesTitleText else maxGradeTitleText,
			gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
			maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
			showsGradeChip = isOverdue
		)
	}
}
