package com.gdavidpb.tuindice.pensum.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.route.PensumRoute
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.pensumEntries(
	shellBindings: NavShellBindings,
	onNavigateToSubjectDetail: (subjectCode: String) -> Unit
) {
	entry<PensumDestination.Pensum> {
		val viewModel = koinViewModel<PensumViewModel>()
		val topBarActionBus = koinInject<PensumTopBarActionBus>()
		val screenSessionStore = koinInject<PensumScreenSessionStore>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectCurrentEntryValueWithLifecycle(
			value = Unit,
			onValue = { viewModel.ensurePensumLoadedAction() }
		)

		PensumRoute(
			topBarActionBus = topBarActionBus,
			screenSessionStore = screenSessionStore,
			onNavigateToSubjectDetail = onNavigateToSubjectDetail,
			viewModel = viewModel
		)
	}
}
