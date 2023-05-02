package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.reducer.collect
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.reducer.EnrollmentProofReducer

class EnrollmentProofViewModel(
	private val enrollmentProofUseCase: FetchEnrollmentProofUseCase,
	private val enrollmentProofReducer: EnrollmentProofReducer
) : BaseViewModel<Enrollment.State, Enrollment.Action, Enrollment.Event>(
	initialViewState = Enrollment.State.Fetching
) {
	fun fetchEnrollmentProofAction() =
		emitAction(Enrollment.Action.FetchEnrollmentProof)

	override suspend fun reducer(action: Enrollment.Action) {
		when (action) {
			is Enrollment.Action.FetchEnrollmentProof ->
				enrollmentProofUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = enrollmentProofReducer)
		}
	}
}