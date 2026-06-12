package $PACKAGE.presentation.transition

import $PACKAGE.presentation.contract.$FEATURE_NAME
import $PACKAGE.presentation.machine.$INTERNAL_EVENT_NAME
import $PACKAGE.presentation.machine.$MACHINE_NAME
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<$FEATURE_NAME.State>.$TRANSITIONS_FUNCTION_NAME(
	machine: $MACHINE_NAME,
	host: MachineHost<$FEATURE_NAME.Effect>
) {
	from<$FEATURE_NAME.State.Loading> {
		on<$FEATURE_NAME.Action.$OBSERVE_ACTION_NAME> { state, _ ->
			machine.startObservation(host = host)
			state
		}
	}

	from<$FEATURE_NAME.State.Content> {
		// Keep content visible while a refresh runs or fails; the snack reports it.
		on<$INTERNAL_EVENT_NAME.$REFRESH_STARTED_EVENT_NAME> { state, _ -> state }

		on<$INTERNAL_EVENT_NAME.$REFRESH_FAILED_EVENT_NAME>(
			emits = setOf($FEATURE_NAME.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect($FEATURE_NAME.Effect.ShowSnackBar(message = event.message))
			state
		}
	}

	fromAny {
		on<$FEATURE_NAME.Action.$REFRESH_ACTION_NAME> { state, _ ->
			machine.refresh(host = host)
			state
		}

		onTo<$INTERNAL_EVENT_NAME.$CONTENT_OBSERVED_EVENT_NAME, $FEATURE_NAME.State.Content> { _, event ->
			$FEATURE_NAME.State.Content(message = event.message)
		}

		onTo<$INTERNAL_EVENT_NAME.$OBSERVATION_FAILED_EVENT_NAME, $FEATURE_NAME.State.Failed>(
			emits = setOf($FEATURE_NAME.Effect.ShowSnackBar::class)
		) { _, event ->
			host.sendEffect($FEATURE_NAME.Effect.ShowSnackBar(message = event.message))
			$FEATURE_NAME.State.Failed
		}

		onTo<$INTERNAL_EVENT_NAME.$REFRESH_STARTED_EVENT_NAME, $FEATURE_NAME.State.Loading> { _, _ ->
			$FEATURE_NAME.State.Loading
		}

		onTo<$INTERNAL_EVENT_NAME.$REFRESH_FAILED_EVENT_NAME, $FEATURE_NAME.State.Failed>(
			emits = setOf($FEATURE_NAME.Effect.ShowSnackBar::class)
		) { _, event ->
			host.sendEffect($FEATURE_NAME.Effect.ShowSnackBar(message = event.message))
			$FEATURE_NAME.State.Failed
		}
	}
}
