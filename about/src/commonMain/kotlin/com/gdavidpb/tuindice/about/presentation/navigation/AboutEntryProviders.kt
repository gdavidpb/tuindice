package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.dialog.DeleteAssistantConversationsDialog
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.aboutEntries(
	shellBindings: NavShellBindings,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onNavigateToAssistantMemory: () -> Unit = {},
	onNavigateToDeleteConfirmation: () -> Unit = {}
) {
	entry<AboutDestination.About> {
		val viewModel = koinViewModel<AboutViewModel>()
		val shareTextHandler = koinInject<ShareTextHandler>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<DeleteAssistantConversationsConfirmationResult> { result ->
			when (result) {
				DeleteAssistantConversationsConfirmationResult.Confirmed ->
					viewModel.confirmDeleteAssistantConversationsAction()
			}
		}

		CompositionLocalProvider(
			LocalShareTextHandler provides shareTextHandler
		) {
			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser,
				onNavigateToAssistantMemory = onNavigateToAssistantMemory,
				onNavigateToDeleteConfirmation = onNavigateToDeleteConfirmation,
				showSnackBar = shellBindings.showSnackBar,
				viewModel = viewModel
			)
		}
	}
}

// The dialogs of the About screen, answered through the navigator of the host.
fun EntryProviderScope<NavKey>.aboutDialogEntries(navActions: TuIndiceNavActions) {
	entry<AboutDestination.DeleteAssistantConversationsDialog>(metadata = dialogMetadata()) {
		DeleteAssistantConversationsDialog(
			onConfirmClick = { navActions.popWithResult(DeleteAssistantConversationsConfirmationResult.Confirmed) },
			onDismissRequest = { navActions.pop() }
		)
	}
}
