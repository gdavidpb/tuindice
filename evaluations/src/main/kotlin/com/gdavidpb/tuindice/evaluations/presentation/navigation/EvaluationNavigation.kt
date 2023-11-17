package com.gdavidpb.tuindice.evaluations.presentation.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute

data class EvaluationArgs(
	val evaluationId: String
)

fun NavController.navigateToEvaluation(args: EvaluationArgs? = null) {
	val title = if (args == null)
		context.getString(R.string.title_add_evaluation)
	else
		context.getString(R.string.title_edit_evaluation)

	navigate("${Destination.Evaluation.route}/${Uri.encode(title)}?evaluationId=${args?.evaluationId}")
}

fun NavGraphBuilder.evaluationScreen(
	navigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable(
		route = "${Destination.Evaluation.route}/{title}?evaluationId={evaluationId}",
		arguments = listOf(
			navArgument("title") { type = NavType.StringType },
			navArgument("evaluationId") { type = NavType.StringType; nullable = true }
		)
	) { backStackEntry ->
		EvaluationRoute(
			evaluationId = backStackEntry.arguments?.getString("evaluationId"),
			onNavigateToEvaluations = navigateToEvaluations,
			showSnackBar = showSnackBar
		)
	}
}