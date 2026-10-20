package com.gdavidpb.tuindice.assistant.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.route.AssistantChatRoute
import com.gdavidpb.tuindice.assistant.presentation.route.AssistantComponentSheetRoute
import com.gdavidpb.tuindice.assistant.presentation.route.AssistantMemoryRoute
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantMemoryViewModel
import com.gdavidpb.tuindice.assistant.ui.dialog.ClearAssistantMemoryDialog
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.assistantEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	dependencies: AssistantNavDependencies
) {
	chatEntry(navActions = navActions, dependencies = dependencies)
	componentSheetEntry(navActions = navActions, dependencies = dependencies)
	memoryEntry(navActions = navActions, shellBindings = shellBindings)
	clearMemoryConfirmationDialogEntry(navActions = navActions)
}

// The chat sheet shows its notices on its own host, so it takes no snackbar from the shell.
private fun EntryProviderScope<NavKey>.chatEntry(
	navActions: TuIndiceNavActions,
	dependencies: AssistantNavDependencies
) {
	entry<AssistantDestination.Chat>(metadata = dialogMetadata()) { key ->
		AssistantChatRoute(
			viewModel = koinViewModel<AssistantChatViewModel>(),
			context = key.context,
			navActions = navActions,
			dependencies = dependencies
		)
	}
}

private fun EntryProviderScope<NavKey>.componentSheetEntry(
	navActions: TuIndiceNavActions,
	dependencies: AssistantNavDependencies
) {
	entry<AssistantDestination.ComponentSheet>(metadata = dialogMetadata()) { key ->
		AssistantComponentSheetRoute(
			viewModel = koinViewModel<AssistantComponentSheetViewModel>(),
			messageId = key.messageId,
			componentId = key.componentId,
			onDismissRequest = { navActions.pop() },
			onNavigateTo = dependencies.onNavigateTo
		)
	}
}

private fun EntryProviderScope<NavKey>.memoryEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings
) {
	entry<AssistantDestination.Memory> {
		val viewModel = koinViewModel<AssistantMemoryViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		// The bar of the shell reads its title from the state of the screen on top, as Preferencias does.
		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<ClearAssistantMemoryConfirmationResult> { result ->
			when (result) {
				ClearAssistantMemoryConfirmationResult.Confirmed ->
					viewModel.dispatch(AssistantMemory.Action.ConfirmClearAll)
			}
		}

		AssistantMemoryRoute(
			viewModel = viewModel,
			onClearAllConfirmationRequested = { navActions.push(AssistantDestination.ClearMemoryConfirmationDialog) },
			showSnackBar = shellBindings.showSnackBar
		)
	}
}

private fun EntryProviderScope<NavKey>.clearMemoryConfirmationDialogEntry(
	navActions: TuIndiceNavActions
) {
	entry<AssistantDestination.ClearMemoryConfirmationDialog>(metadata = dialogMetadata()) {
		ClearAssistantMemoryDialog(
			onConfirmClick = { navActions.popWithResult(ClearAssistantMemoryConfirmationResult.Confirmed) },
			onDismissRequest = { navActions.pop() }
		)
	}
}
