package com.gdavidpb.tuindice.enrollmentproof.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofInternalEvent
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine

internal fun MachineDefinitionBuilder<Enrollment.State>.enrollmentProofTransitions(
	machine: EnrollmentProofMachine,
	host: MachineHost<Enrollment.Effect>
) {
	from<Enrollment.State.Fetching> {
		on<Enrollment.Action.FetchEnrollmentProof> { state, _ ->
			machine.fetch(host = host)
			state
		}

		on<EnrollmentProofInternalEvent.EnrollmentProofFetched>(
			emits = setOf(Enrollment.Effect.OpenEnrollmentProof::class)
		) { state, event ->
			host.sendEffect(Enrollment.Effect.OpenEnrollmentProof(file = event.file))
			state
		}

		on<EnrollmentProofInternalEvent.EnrollmentProofFetchFailed>(
			emits = setOf(Enrollment.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Enrollment.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<EnrollmentProofInternalEvent.EnrollmentProofUnauthorized>(
			emits = setOf(Enrollment.Effect.NavigateToOutdatedCredentials::class)
		) { state, _ ->
			host.sendEffect(Enrollment.Effect.NavigateToOutdatedCredentials)
			state
		}
	}
}
