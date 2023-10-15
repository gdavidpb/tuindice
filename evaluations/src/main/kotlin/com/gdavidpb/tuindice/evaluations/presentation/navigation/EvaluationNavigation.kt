package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.route.AddEvaluationRoute

data class EvaluationArgs(
	val evaluationId: String
)

fun NavController.navigateToAddEvaluation() {
	navigate(Destination.AddEvaluation.route)
}

fun NavController.navigateToEvaluation(args: EvaluationArgs) {
	TODO()
}

fun NavGraphBuilder.addEvaluationScreen(
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	composable(Destination.AddEvaluation.route) {
		AddEvaluationRoute(
			showSnackBar = showSnackBar
		)
	}
}