package com.gdavidpb.tuindice.pensum.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.top_bar_pensum

object Pensum {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_pensum),
		override val topBarConfig: TopBarConfig = TopBarConfig.Pensum,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Idle : State()

		data object Loading : State()

		data object Empty : State()

		data class Content(
			val model: PensumScreenModel,
			val isRefreshing: Boolean = false
		) : State()

		data class Failed(
			val message: UiText = UiText.Resource(Res.string.pensum_failed_message)
		) : State()
	}

	sealed class Action : ViewAction() {
		data object ObservePensum : Action()
		data object RefreshPensum : Action()
		class SelectPensum(
			val year: Int
		) : Action()
		class SelectModality(
			val modalityId: String
		) : Action()
		class SelectSelection(
			val year: Int,
			val modalityId: String
		) : Action()
	}

	sealed class Effect : ViewEffect() {
		class ShowSnackBar(val message: UiText) : Effect()
	}
}
