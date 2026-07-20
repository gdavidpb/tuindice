package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.presentation.route.BrowserRoute
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.cancel
import tuindice.maincore.generated.resources.dialog_message_warning_external
import tuindice.maincore.generated.resources.dialog_title_warning_external
import tuindice.maincore.generated.resources.open

/* Nav2 suppressed every Browser transition explicitly; per-entry metadata is
   the Nav3 equivalent. */
private val browserTransitionMetadata: Map<String, Any> =
	NavDisplay.transitionSpec {
		ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
	} + NavDisplay.popTransitionSpec {
		ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
	} + NavDisplay.predictivePopTransitionSpec { _ ->
		ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)
	}

fun EntryProviderScope<NavKey>.browserEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	onNavigateToExternalResource: (url: String) -> Unit
) {
	entry<BrowserDestination.Browser>(metadata = browserTransitionMetadata) { key ->
		val viewModel = koinViewModel<BrowserViewModel>()
		val renderer = koinInject<BrowserScreenRenderer>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		BrowserRoute(
			title = key.title,
			url = key.url,
			onNavigateToExternalResourceDialog = { url ->
				navActions.push(BrowserDestination.ExternalResourceDialog(url = url))
			},
			viewModel = viewModel,
			renderer = renderer
		)
	}

	entry<BrowserDestination.ExternalResourceDialog>(metadata = dialogMetadata()) { key ->
		ExternalResourceDialog(
			url = key.url,
			titleText = stringResource(Res.string.dialog_title_warning_external),
			messageText = stringResource(Res.string.dialog_message_warning_external),
			openText = stringResource(Res.string.open),
			cancelText = stringResource(Res.string.cancel),
			onConfirmClick = onNavigateToExternalResource,
			onDismissRequest = { navActions.pop() }
		)
	}
}
