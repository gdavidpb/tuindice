package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.navigatePopUpTo
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute

fun NavController.navigateToEvaluations() {
	navigatePopUpTo(Destination.Evaluations)
}

fun NavGraphBuilder.evaluationsScreen(
	navigateToAddEvaluation: () -> Unit,
	navigateToEvaluation: (args: EvaluationArgs) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable(Destination.Evaluations.route) {
		EvaluationsRoute(
			onNavigateToAddEvaluation = navigateToAddEvaluation,
			onNavigateToEvaluation = { evaluationId ->
				navigateToEvaluation(
					EvaluationArgs(
						evaluationId = evaluationId
					)
				)
			},
			showSnackBar = showSnackBar
		)
	}
}