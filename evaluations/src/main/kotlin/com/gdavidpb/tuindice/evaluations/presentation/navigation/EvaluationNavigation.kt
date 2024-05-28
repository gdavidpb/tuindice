package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute

fun NavController.navigateToEvaluation(evaluationId: String? = null) {
	val title = if (evaluationId == null)
		context.getString(R.string.title_add_evaluation)
	else
		context.getString(R.string.title_edit_evaluation)

	navigate(
		Destination.Evaluation(
			title = title,
			evaluationId = evaluationId
		)
	)
}

fun NavGraphBuilder.evaluationScreen(
	navigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable<Destination.Evaluation> { backStackEntry ->
		val destination = backStackEntry.toRoute<Destination.Evaluation>()

		EvaluationRoute(
			evaluationId = destination.evaluationId,
			onNavigateToEvaluations = navigateToEvaluations,
			showSnackBar = showSnackBar
		)
	}
}