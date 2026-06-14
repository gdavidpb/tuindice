package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine

class EnrollmentProofViewModel(
	override val screenMachine: EnrollmentProofMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Enrollment.State, Enrollment.Action, Enrollment.Effect>(
	name = "enrollment_proof",
	initialState = screenMachine.initialState(),
	initialAction = Enrollment.Action.FetchEnrollmentProof,
	dispatchers = dispatchers
)
