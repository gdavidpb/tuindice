package com.gdavidpb.tuindice.subjects.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.top_bar_subject_search

object SubjectSearch {
	data class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_subject_search),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false,
		val query: String = "",
		val results: List<SubjectSearchResultItem> = emptyList(),
		val isRefreshing: Boolean = false,
		val hasRemoteError: Boolean = false
	) : ViewState

	sealed class Action : ViewAction {
		data object ObserveSubjectSearch : Action()
		data class UpdateQuery(val query: String) : Action()
		data object Retry : Action()
	}

	sealed class Effect : ViewEffect
}
