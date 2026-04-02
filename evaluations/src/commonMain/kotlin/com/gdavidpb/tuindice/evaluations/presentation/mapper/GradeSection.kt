package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import org.jetbrains.compose.resources.getString
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
	val overdueTitleText = stringResource(Res.string.label_add_evaluation_grades)

	return remember(
		isOverdue,
		grade,
		maxGrade,
		maxGradeTitleText,
		overdueTitleText
	) {
		createEvaluationGradeSectionItem(
			maxGradeTitleText = maxGradeTitleText,
			overdueTitleText = overdueTitleText,
			isOverdue = isOverdue,
			grade = grade,
			maxGrade = maxGrade
		)
	}
}

suspend fun getEvaluationGradeSectionItem(
	isOverdue: Boolean,
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return createEvaluationGradeSectionItem(
		maxGradeTitleText = getString(Res.string.label_add_evaluation_max_grade),
		overdueTitleText = getString(Res.string.label_add_evaluation_grades),
		isOverdue = isOverdue,
		grade = grade,
		maxGrade = maxGrade
	)
}

fun EvaluationGradeSectionItem.updated(
	isOverdue: Boolean,
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return copy(
		gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
		maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
		showsGradeChip = isOverdue
	)
}

private fun createEvaluationGradeSectionItem(
	maxGradeTitleText: String,
	overdueTitleText: String,
	isOverdue: Boolean,
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return EvaluationGradeSectionItem(
		maxGradeTitleText = maxGradeTitleText,
		overdueTitleText = overdueTitleText,
		gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
		maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
		showsGradeChip = isOverdue
	)
}
