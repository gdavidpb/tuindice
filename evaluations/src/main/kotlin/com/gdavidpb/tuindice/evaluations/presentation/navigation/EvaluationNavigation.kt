package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

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
	navigation(
		route = Destination.AddEvaluation.route,
		startDestination = Destination.AddEvaluation.Step1.route
	) {
		composable(Destination.AddEvaluation.Step1.route) {
			TODO()
		}

		composable(Destination.AddEvaluation.Step2.route) {
			TODO()
		}
	}
}