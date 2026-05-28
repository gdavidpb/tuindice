package com.gdavidpb.tuindice.evaluations.presentation.model

data class EvaluationGradeSectionItem(
	val maxGradeTitleText: String,
	val overdueTitleText: String,
	val gradeText: String,
	val maxGradeText: String,
	val showsGradeChip: Boolean
) {
	val titleText: String
		get() = if (showsGradeChip) overdueTitleText else maxGradeTitleText
}
