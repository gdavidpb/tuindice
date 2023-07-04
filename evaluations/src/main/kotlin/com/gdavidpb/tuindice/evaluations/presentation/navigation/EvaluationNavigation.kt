package com.gdavidpb.tuindice.evaluations.presentation.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute

data class EvaluationArgs(
	val title: String,
	val evaluationId: String? = null
)

fun NavController.navigateToAddEvaluation(args: EvaluationArgs) {
	val title = Uri.encode(args.title)

	navigate("${Destination.Evaluation.route}/$title")
}

fun NavController.navigateToEvaluation(args: EvaluationArgs) {
	val title = Uri.encode(args.title)
	val evaluationId = args.evaluationId

	requireNotNull(evaluationId)

	navigate("${Destination.Evaluation.route}/$title?evaluationId=$evaluationId")
}

fun NavGraphBuilder.evaluationScreen(
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	composable(
		route = "${Destination.Evaluation.route}/{title}?evaluationId={evaluationId}",
		arguments = listOf(
			navArgument("title") {
				type = NavType.StringType
			},
			navArgument("evaluationId") {
				type = NavType.StringType
				defaultValue = ""
			}
		)
	) { backStackEntry ->
		EvaluationRoute(
			evaluationId = backStackEntry.arguments?.getString("evaluationId") ?: "",
			showSnackBar = showSnackBar
		)
	}
}