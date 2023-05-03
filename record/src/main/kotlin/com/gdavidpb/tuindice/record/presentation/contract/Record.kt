package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import com.gdavidpb.tuindice.record.presentation.model.RecordViewState

object Record {
	sealed class State : ViewState {
		object Loading : State()
		class Loaded(val value: RecordViewState) : State()
		object Failed : State()
	}

	sealed class Action : ViewAction {
		object LoadQuarters : Action()
		class UpdateSubject(val params: UpdateSubjectParams) : Action()
		class WithdrawSubject(val params: WithdrawSubjectParams) : Action()
		class RemoveQuarter(val params: RemoveQuarterParams) : Action()
		object OpenEnrollmentProof : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateToAccountDisabled : Event()
		object NavigateToOutdatedPassword : Event()
		object NavigateToEnrollmentProof : Event()
		object ShowTimeoutSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowUnavailableSnackBar : Event()
		object ShowDefaultErrorSnackBar : Event()
	}
}