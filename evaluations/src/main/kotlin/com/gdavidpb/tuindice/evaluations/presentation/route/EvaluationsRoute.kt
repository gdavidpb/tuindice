package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen

@Composable
fun EvaluationsRoute(
	onNavigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	EvaluationsScreen()
}