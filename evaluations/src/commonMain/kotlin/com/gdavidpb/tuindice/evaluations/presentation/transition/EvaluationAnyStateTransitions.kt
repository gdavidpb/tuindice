package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationMachine

internal fun MachineDefinitionBuilder<Evaluation.State>.evaluationAnyStateTransitions(
	machine: EvaluationMachine,
	host: MachineHost<Evaluation.Effect>
) {
	fromAny {
		// Loading may (re)start from any state: the route dispatches it at startup and
		// the retry button re-dispatches it from Failed; both replace whatever is on
		// screen through the load lifecycle events.
		on<Evaluation.Action.LoadAvailableAttempts> { state, _ ->
			machine.loadAvailableAttempts(host = host)
			state
		}

		on<Evaluation.Action.LoadEvaluation> { state, action ->
			machine.loadEvaluation(host = host, action = action)
			state
		}

		onTo<EvaluationInternalEvent.EditorLoadStarted, Evaluation.State.Loading> { _, _ ->
			Evaluation.State.Loading
		}

		onTo<EvaluationInternalEvent.EditorContentLoaded, Evaluation.State.Content> { _, event ->
			event.content
		}

		onTo<EvaluationInternalEvent.EditorLoadFailed, Evaluation.State.Failed> { _, _ ->
			Evaluation.State.Failed
		}
	}
}
