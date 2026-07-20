package com.gdavidpb.tuindice.sampleflow.presentation.route

import com.gdavidpb.tuindice.sampleflow.presentation.machine.SampleFlowDraft
import org.koin.compose.viewmodel.koinViewModel

object RogueRouteAction : ViewAction

fun sampleFlowRoute(draft: SampleFlowDraft, navController: Any): Any {
	val viewModel = koinViewModel<Any>()
	return viewModel to (draft to navController)
}

fun EntryProviderScope<Any>.rogueEntries(viewModel: Any): Any = viewModel
