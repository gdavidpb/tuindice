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
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.aboutEntries(
	shellBindings: NavShellBindings,
	onNavigateToBrowser: (title: String, url: String) -> Unit
) {
	entry<AboutDestination.About> {
		val viewModel = koinViewModel<AboutViewModel>()
		val shareTextHandler = koinInject<ShareTextHandler>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CompositionLocalProvider(
			LocalShareTextHandler provides shareTextHandler
		) {
			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser,
				showSnackBar = shellBindings.showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
