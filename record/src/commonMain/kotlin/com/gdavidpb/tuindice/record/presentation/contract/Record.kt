package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

object Record {
	sealed class State(
		override val topBarTitle: String = "Informe Académico",
		override val topBarConfig: TopBarConfig = TopBarConfig.Record,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Loading : State()

		data class Content(
			val quarters: List<Quarter>,
			val selectedQuarterId: String
		) : State()

		data object Empty : State()

		data object Failed : State()
	}

	sealed class Action : ViewAction() {
		data object ObserveQuarters : Action()
		data object RefreshQuarters : Action()
		class SelectQuarter(val quarterId: String) : Action()

		class SetSubjectGrade(
			val quarterId: String,
			val subjectId: String,
			val grade: Int,
			val commit: Boolean
		) : Action()
	}

	sealed class Effect : ViewEffect() {
		data object NavigateToOutdatedCredentials : Effect()
		class ShowSnackBar(val message: String) : Effect()
	}
}
