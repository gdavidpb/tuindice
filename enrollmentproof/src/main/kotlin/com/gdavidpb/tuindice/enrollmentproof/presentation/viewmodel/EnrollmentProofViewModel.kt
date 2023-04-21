package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.reducer.EnrollmentProofReducer

class EnrollmentProofViewModel(
	private val enrollmentProofReducer: EnrollmentProofReducer
) : BaseViewModel<Enrollment.State, Enrollment.Action, Enrollment.Event>(
	initialViewState = Enrollment.State.Fetching
) {
	fun fetchEnrollmentProofAction() =
		emitAction(Enrollment.Action.FetchEnrollmentProof)

	override suspend fun reducer(action: Enrollment.Action) {
		when (action) {
			is Enrollment.Action.FetchEnrollmentProof ->
				enrollmentProofReducer.reduce(
					action = action,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)
		}
	}
}