package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade

suspend fun getEvaluationGradeSectionItem(
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return createEvaluationGradeSectionItem(
		maxGradeTitleText = getString(Res.string.label_add_evaluation_max_grade),
		overdueTitleText = getString(Res.string.label_add_evaluation_grades),
		grade = grade,
		maxGrade = maxGrade
	)
}

fun EvaluationGradeSectionItem.updated(
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return copy(
		gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
		maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
		showsGradeChip = shouldShowGradeChip(maxGrade)
	)
}

private fun createEvaluationGradeSectionItem(
	maxGradeTitleText: String,
	overdueTitleText: String,
	grade: Double?,
	maxGrade: Double?
): EvaluationGradeSectionItem {
	return EvaluationGradeSectionItem(
		maxGradeTitleText = maxGradeTitleText,
		overdueTitleText = overdueTitleText,
		gradeText = (grade ?: 0.0).formatGrade(decimals = 2),
		maxGradeText = (maxGrade ?: 0.0).formatGrade(decimals = 2),
		showsGradeChip = shouldShowGradeChip(maxGrade)
	)
}

// The list row lets the user grade any evaluation, so the editor mirrors that
// rule: a usable max grade is the only requirement, overdue or not.
private fun shouldShowGradeChip(
	maxGrade: Double?
): Boolean =
	maxGrade != null && maxGrade > 0.0
