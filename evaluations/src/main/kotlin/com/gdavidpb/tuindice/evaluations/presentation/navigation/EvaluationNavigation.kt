package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.route.AddEvaluationRoute

data class EvaluationArgs(
	val evaluationId: String
)

fun NavController.navigateToAddEvaluation(subRoute: Destination.AddEvaluation) {
	navigate(subRoute.route)
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
			AddEvaluationRoute(
				subRoute = Destination.AddEvaluation.Step1,
				showSnackBar = showSnackBar
			)
		}

		composable(Destination.AddEvaluation.Step2.route) {
			AddEvaluationRoute(
				subRoute = Destination.AddEvaluation.Step2,
				showSnackBar = showSnackBar
			)
		}
	}
}