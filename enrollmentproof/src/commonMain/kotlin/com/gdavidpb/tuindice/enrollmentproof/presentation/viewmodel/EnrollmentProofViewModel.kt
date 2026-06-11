package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import kotlinx.coroutines.flow.Flow

class EnrollmentProofViewModel(
	private val enrollmentProofActionProcessor: FetchEnrollmentProofActionProcessor,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : BaseViewModel<Enrollment.State, Enrollment.Action, Enrollment.Effect>(
	name = "enrollment_proof",
	initialState = Enrollment.State.Fetching,
	initialAction = Enrollment.Action.FetchEnrollmentProof,
	dispatchers = dispatchers
) {
	override suspend fun processAction(
		action: Enrollment.Action,
		sideEffect: (Enrollment.Effect) -> Unit
	): Flow<Mutation<Enrollment.State>> {
		return when (action) {
			is Enrollment.Action.FetchEnrollmentProof ->
				enrollmentProofActionProcessor.process(action, sideEffect)
		}
	}
}
