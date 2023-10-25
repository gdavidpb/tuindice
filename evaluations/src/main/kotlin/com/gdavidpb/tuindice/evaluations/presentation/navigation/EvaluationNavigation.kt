package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.route.AddEvaluationRoute

data class EvaluationArgs(
	val evaluationId: String
)

fun NavController.navigateToAddEvaluation() {
	navigate(Destination.AddEvaluation.route)
}

fun NavController.navigateToEvaluation(args: EvaluationArgs) {
	// TODO
}

fun NavGraphBuilder.addEvaluationScreen(
	navigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable(Destination.AddEvaluation.route) {
		AddEvaluationRoute(
			onNavigateToEvaluations = navigateToEvaluations,
			showSnackBar = showSnackBar
		)
	}
}