package com.gdavidpb.tuindice.enrollmentproof.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofInternalEvent
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine

internal fun MachineDefinitionBuilder<Enrollment.State>.anyStateTransitions(
	machine: EnrollmentProofMachine,
	host: MachineHost<Enrollment.Effect>
) {
	fromAny {
		// The proof is opened from either state (the fresh download, or the saved copy once
		// confirmed), so its outcome is accepted in both. Opening the viewer is the route's;
		// what to say when the device has none is decided here.
		on<Enrollment.Action.OpenEnrollmentProofCompleted> { state, action ->
			if (!action.opened) {
				machine.reportViewerMissing(host = host)
			}

			state
		}

		// Installing a viewer is not something a retry of the download fixes.
		on<EnrollmentProofInternalEvent.EnrollmentProofViewerMissing>(
			emits = setOf(Enrollment.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(
				Enrollment.Effect.ShowSnackBar(message = event.message, canRetry = false)
			)
			state
		}
	}
}
