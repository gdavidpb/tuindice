package com.gdavidpb.tuindice.record.presentation.contract

import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import com.gdavidpb.tuindice.record.presentation.model.RecordViewState
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem

object Record {
	sealed class State {
		object Loading : State()
		class Loaded(val value: RecordViewState) : State()
		object Failed : State()
	}

	sealed class Action {
		object LoadQuarters : Action()
		class UpdateSubject(val params: UpdateSubjectParams) : Action()
		class WithdrawSubject(val params: WithdrawSubjectParams) : Action()
		class RemoveQuarter(val params: RemoveQuarterParams) : Action()
		class OpenEvaluationPlan(val item: SubjectItem) : Action()
		object OpenEnrollmentProof : Action()
	}

	sealed class Event {
		object NavigateToAccountDisabled : Event()
		object NavigateToOutdatedPassword : Event()
		object NavigateToEnrollmentProof : Event()
		class NavigateToEvaluationPlan(val item: SubjectItem) : Event()
		object ShowTimeoutSnackBar : Event()
		class ShowNoConnectionSnackBar(val isNetworkAvailable: Boolean) : Event()
		object ShowUnavailableSnackBar : Event()
		object ShowDefaultErrorSnackBar : Event()
	}
}