package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_add_evaluation_grades
import tuindice.evaluations.generated.resources.label_add_evaluation_max_grade

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
		gradeText = grade.formatScoreText(maxGrade),
		maxGradeText = maxGrade.formatWeightText(),
		showsGradeChip = shouldShowGradeChip(isOverdue, maxGrade)
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
		gradeText = grade.formatScoreText(maxGrade),
		maxGradeText = maxGrade.formatWeightText(),
		showsGradeChip = shouldShowGradeChip(isOverdue, maxGrade)
	)
}

private fun shouldShowGradeChip(
	isOverdue: Boolean,
	maxGrade: Double?
): Boolean =
	isOverdue && maxGrade != null && maxGrade > 0.0

private fun Double?.formatScoreText(maxGrade: Double?): String {
	val maxGradeText = maxGrade?.formatCompactGrade() ?: "--"

	return this?.let { grade ->
		"${grade.formatCompactGrade()} / $maxGradeText"
	} ?: "-- / $maxGradeText"
}

private fun Double?.formatWeightText(): String {
	return this?.let { weight ->
		weight.formatCompactGrade()
	} ?: "--"
}

private fun Double.formatCompactGrade(): String {
	return if (this == toInt().toDouble()) {
		formatGrade(decimals = 0)
	} else {
		formatGrade(decimals = 2)
	}
}
