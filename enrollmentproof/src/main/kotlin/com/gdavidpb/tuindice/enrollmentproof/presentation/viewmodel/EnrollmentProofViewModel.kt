package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.processor.EnrollmentProcessor

class EnrollmentProofViewModel(
	private val fetchEnrollmentProofUseCase: FetchEnrollmentProofUseCase
) : BaseViewModel<Enrollment.State, Enrollment.Action, Enrollment.Event>(initialViewState = Enrollment.State.Fetching) {
	fun fetchEnrollmentProofAction() =
		emitAction(Enrollment.Action.FetchEnrollmentProof)

	override suspend fun handleAction(action: Enrollment.Action) {
		when (action) {
			Enrollment.Action.FetchEnrollmentProof ->
				execute(
					useCase = fetchEnrollmentProofUseCase,
					params = Unit,
					processor = EnrollmentProcessor { event -> sendEvent(event) }
				)
		}
	}
}