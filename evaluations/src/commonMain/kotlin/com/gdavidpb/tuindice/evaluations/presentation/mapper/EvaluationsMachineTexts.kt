package com.gdavidpb.tuindice.evaluations.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluations_continuous_label
import tuindice.evaluations.generated.resources.evaluations_week_label
import tuindice.evaluations.generated.resources.snack_evaluation_added
import tuindice.evaluations.generated.resources.snack_evaluation_removed
import tuindice.evaluations.generated.resources.snack_evaluation_set_grade
import tuindice.evaluations.generated.resources.snack_evaluation_updated

internal suspend fun evaluationsContinuousLabel(): String {
	return getString(Res.string.evaluations_continuous_label)
}

internal suspend fun evaluationsWeekLabel(): String {
	return getString(Res.string.evaluations_week_label)
}

internal suspend fun snackEvaluationAdded(): String {
	return getString(Res.string.snack_evaluation_added)
}

internal suspend fun snackEvaluationRemoved(): String {
	return getString(Res.string.snack_evaluation_removed)
}

internal suspend fun snackEvaluationSetGrade(): String {
	return getString(Res.string.snack_evaluation_set_grade)
}

internal suspend fun snackEvaluationUpdated(): String {
	return getString(Res.string.snack_evaluation_updated)
}
