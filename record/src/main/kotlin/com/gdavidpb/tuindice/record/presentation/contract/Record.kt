package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams

object Record {
	sealed class State : ViewState {
		object Loading : State()

		data class Content(
			val quarters: List<Quarter>
		) : State()

		object Empty : State()

		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadQuarters : Action()
		class UpdateSubject(val params: UpdateSubjectParams) : Action()
		class WithdrawSubject(val params: WithdrawSubjectParams) : Action()
		class RemoveQuarter(val params: RemoveQuarterParams) : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToOutdatedPassword : Event()
		class ShowSnackBar(val message: String) : Event()
	}
}