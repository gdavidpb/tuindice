package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute

fun NavController.navigateToEvaluations() {
	navigate(Destination.Evaluations.route)
}

fun NavGraphBuilder.evaluationsScreen(
	navigateToAddEvaluation: (args: EvaluationArgs) -> Unit,
	navigateToEvaluation: (args: EvaluationArgs) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	composable(Destination.Evaluations.route) {
		EvaluationsRoute(
			onNavigateToAddEvaluation = { title ->
				navigateToAddEvaluation(
					EvaluationArgs(
						title = title
					)
				)
			},
			onNavigateToEvaluation = { title, evaluationId ->
				navigateToEvaluation(
					EvaluationArgs(
						title = title,
						evaluationId = evaluationId
					)
				)
			},
			showSnackBar = showSnackBar
		)
	}
}