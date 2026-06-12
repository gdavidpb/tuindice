package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine

internal fun MachineDefinitionBuilder<Summary.State>.anyStateTransitions(
	machine: SummaryMachine,
	host: MachineHost<Summary.Effect>
) {
	fromAny {
		on<Summary.Action.TakeProfilePicture>(
			emits = setOf(Summary.Effect.OpenCamera::class)
		) { state, _ ->
			host.sendEffect(Summary.Effect.OpenCamera)
			state
		}

		on<Summary.Action.PickProfilePicture>(
			emits = setOf(Summary.Effect.OpenPicker::class)
		) { state, _ ->
			host.sendEffect(Summary.Effect.OpenPicker)
			state
		}

		on<Summary.Action.RemoveProfilePicture>(
			emits = setOf(Summary.Effect.ShowRemoveProfilePictureConfirmationDialog::class)
		) { state, _ ->
			host.sendEffect(Summary.Effect.ShowRemoveProfilePictureConfirmationDialog)
			state
		}

		on<Summary.Action.UploadProfilePicture> { state, action ->
			machine.uploadProfilePicture(host = host, file = action.file)
			state
		}

		on<Summary.Action.ConfirmRemoveProfilePicture> { state, _ ->
			machine.removeProfilePicture(host = host)
			state
		}

		onTo<SummaryInternalEvent.UserObserved, Summary.State.Content> { state, event ->
			event.content.copy(isUserRefreshing = state.isUserRefreshing)
		}

		onTo<SummaryInternalEvent.ObservationFailed, Summary.State.Failed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			Summary.State.Failed(isUserRefreshing = state.isUserRefreshing)
		}

		onTo<SummaryInternalEvent.RefreshFailed, Summary.State.Failed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { _, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			Summary.State.Failed()
		}

		on<SummaryInternalEvent.ProfilePictureUploadFailed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			if (state is Summary.State.Content) {
				state.copy(isProfilePictureLoading = false)
			} else {
				state
			}
		}
	}
}
