package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationRoute(
	evaluationId: String,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: EvaluationViewModel = koinViewModel()
) {
	EvaluationScreen()
}