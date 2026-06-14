package com.gdavidpb.tuindice.enrollmentproof.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.mapper.toErrorMessage
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.transition.enrollmentProofTransitions

class EnrollmentProofMachine(
	private val fetchEnrollmentProofUseCase: FetchEnrollmentProofUseCase,
	private val textProvider: EnrollmentProofTextProvider
) : ScreenMachine<Enrollment.State, Enrollment.Effect> {
	override fun initialState(): Enrollment.State = Enrollment.State.Fetching

	override fun define(host: MachineHost<Enrollment.Effect>): MachineDefinition<Enrollment.State> {
		return MachineDefinition.define {
			enrollmentProofTransitions(machine = this@EnrollmentProofMachine, host = host)
		}
	}

	internal fun fetch(host: MachineHost<Enrollment.Effect>) {
		host.launchMachineJob {
			fetchEnrollmentProofUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						EnrollmentProofInternalEvent.EnrollmentProofFetched(
							file = useCaseState.value
						)
					)

					is UseCaseState.Error -> when (useCaseState.error) {
						is FetchEnrollmentProofUseCaseError.OutdatedCredentials ->
							host.processInternalEvent(
								EnrollmentProofInternalEvent.EnrollmentProofUnauthorized
							)

						else -> host.processInternalEvent(
							EnrollmentProofInternalEvent.EnrollmentProofFetchFailed(
								message = useCaseState.error.toErrorMessage(textProvider)
							)
						)
					}
				}
			}
		}
	}
}
