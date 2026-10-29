package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.route.SettingsRoute
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
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
	settings: SettingsNavDependencies
) {
	entry<AboutDestination.Settings> {
		SettingsEntry(shellBindings = shellBindings, settings = settings)
	}

	entry<AboutDestination.About> {
		val viewModel = koinViewModel<AboutViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		AboutRoute(
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

@Composable
private fun SettingsEntry(
	shellBindings: NavShellBindings,
	settings: SettingsNavDependencies
) {
	val viewModel = koinViewModel<SettingsViewModel>()
	val shareTextHandler = koinInject<ShareTextHandler>()
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectCurrentEntryValueWithLifecycle(
		value = viewState,
		onValue = shellBindings.onViewStateChanged
	)

	CollectNavResultWithLifecycle<DeleteAssistantConversationsConfirmationResult> { result ->
		when (result) {
			DeleteAssistantConversationsConfirmationResult.Confirmed ->
				viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)
		}
	}

	CompositionLocalProvider(
		LocalShareTextHandler provides shareTextHandler
	) {
		SettingsRoute(
			dependencies = settings,
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

// The dialogs of Settings, answered through the navigator of the host.
fun EntryProviderScope<NavKey>.aboutDialogEntries(navActions: TuIndiceNavActions) {
	entry<AboutDestination.DeleteAssistantConversationsDialog>(metadata = dialogMetadata()) {
		DeleteAssistantConversationsDialog(
			onConfirmClick = { navActions.popWithResult(DeleteAssistantConversationsConfirmationResult.Confirmed) },
			onDismissRequest = { navActions.pop() }
		)
	}
}
