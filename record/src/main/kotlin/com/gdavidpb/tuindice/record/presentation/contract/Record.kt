package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Record {
	sealed interface State : ViewState {
		data object Loading : State

		data class Content(
			val quarters: List<Quarter>
		) : State

		data object Empty : State

		data object Failed : State
	}

	sealed interface Action : ViewAction {
		data object LoadQuarters : Action

		class UpdateSubject(
			val quarterId: String,
			val subjectId: String,
			val grade: Int,
			val dispatchToRemote: Boolean
		) : Action
	}

	sealed interface Effect : ViewEffect {
		data object NavigateToOutdatedPassword : Effect
		class ShowSnackBar(val message: String) : Effect
	}
}