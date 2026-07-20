package com.gdavidpb.tuindice.subjects.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectDetailRoute
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectSearchRoute
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import org.koin.compose.viewmodel.koinViewModel
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.top_bar_subject_detail

fun EntryProviderScope<NavKey>.subjectsEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings
) {
	entry<SubjectsDestination.SubjectSearch> {
		val viewModel = koinViewModel<SubjectSearchViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		SubjectSearchRoute(
			viewModel = viewModel,
			onSubjectClick = { subjectCode ->
				navActions.push(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
			}
		)
	}

	entry<SubjectsDestination.SubjectDetail> { key ->
		val viewModel = koinViewModel<SubjectDetailViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState.resolveNavigationViewState(subjectCode = key.subjectCode),
			onValue = shellBindings.onViewStateChanged
		)

		SubjectDetailRoute(
			subjectCode = key.subjectCode,
			viewModel = viewModel,
			onDismissRequest = { navActions.pop() }
		)
	}
}

private fun SubjectDetail.State.resolveNavigationViewState(subjectCode: String): ViewState {
	return when (this) {
		SubjectDetail.State.Idle,
		SubjectDetail.State.Loading ->
			object : ViewState {
				override val topBarTitle: UiText = UiText.Resource(
					resource = Res.string.top_bar_subject_detail,
					args = listOf(subjectCode)
				)
				override val isTopBarVisible: Boolean = true
			}

		is SubjectDetail.State.Content,
		is SubjectDetail.State.Failed,
		is SubjectDetail.State.Unavailable,
		-> this
	}
}
