package com.gdavidpb.tuindice.enrollmentproof.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

internal fun MachineDefinitionBuilder<Enrollment.State>.confirmingSavedCopyTransitions(
	host: MachineHost<Enrollment.Effect>
) {
	from<Enrollment.State.ConfirmingSavedCopy> {
		on<Enrollment.Action.OpenSavedEnrollmentProof>(
			emits = setOf(Enrollment.Effect.OpenEnrollmentProof::class)
		) { state, _ ->
			host.sendEffect(Enrollment.Effect.OpenEnrollmentProof(file = state.file))
			state
		}
	}
}
