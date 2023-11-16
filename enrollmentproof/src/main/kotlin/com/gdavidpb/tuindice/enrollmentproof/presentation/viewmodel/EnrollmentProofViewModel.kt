package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import kotlinx.coroutines.flow.Flow

class EnrollmentProofViewModel(
	private val enrollmentProofActionProcessor: FetchEnrollmentProofActionProcessor
) : BaseViewModel<Enrollment.State, Enrollment.Action, Enrollment.Effect>(
	initialState = Enrollment.State.Fetching
) {
	fun fetchEnrollmentProofAction() =
		sendAction(Enrollment.Action.FetchEnrollmentProof)

	override fun processAction(
		action: Enrollment.Action,
		sideEffect: (Enrollment.Effect) -> Unit
	): Flow<Mutation<Enrollment.State>> {
		return when (action) {
			is Enrollment.Action.FetchEnrollmentProof ->
				enrollmentProofActionProcessor.process(action, sideEffect)
		}
	}
}