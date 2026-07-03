package com.gdavidpb.tuindice.sampleflow.presentation.route

import com.gdavidpb.tuindice.sampleflow.presentation.machine.SampleFlowDraft
import org.koin.compose.viewmodel.koinViewModel

object RogueRouteAction : ViewAction

fun sampleFlowRoute(draft: SampleFlowDraft, navController: Any): Any {
	val viewModel = koinViewModel<Any>()
	navController.navigateBackWithResult(RogueRouteAction)
	return viewModel to draft
}
