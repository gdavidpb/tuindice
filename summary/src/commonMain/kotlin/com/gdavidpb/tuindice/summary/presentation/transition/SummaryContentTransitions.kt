package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine

internal fun MachineDefinitionBuilder<Summary.State>.contentTransitions(
	machine: SummaryMachine,
	host: MachineHost<Summary.Effect>
) {
	from<Summary.State.Content> {
		on<Summary.Action.RefreshSummary> { state, _ ->
			machine.refreshUser(host = host)
			state.copy(isUserRefreshing = true)
		}

		on<Summary.Action.OpenProfilePictureSettings>(
			emits = setOf(Summary.Effect.ShowProfilePictureSettingsDialog::class)
		) { state, _ ->
			if (!state.isUserRefreshing && !state.isProfilePictureLoading) {
				host.sendEffect(
					Summary.Effect.ShowProfilePictureSettingsDialog(
						showRemove = state.profilePictureUrl.isNotEmpty()
					)
				)
			}

			state
		}

		on<SummaryInternalEvent.UserObserved> { state, event ->
			event.content.copy(
				isProfilePictureLoading = state.isProfilePictureLoading,
				isUserRefreshing = state.isUserRefreshing
			)
		}

		on<SummaryInternalEvent.ObservationFailed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(isUserRefreshing = false)
		}

		on<SummaryInternalEvent.RefreshSucceeded> { state, _ ->
			state.copy(isUserRefreshing = false)
		}

		on<SummaryInternalEvent.RefreshFailed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(isUserRefreshing = false)
		}

		on<SummaryInternalEvent.ProfilePictureUploadStarted> { state, _ ->
			state.copy(isProfilePictureLoading = true)
		}

		on<SummaryInternalEvent.ProfilePictureUploadSucceeded>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(isProfilePictureLoading = false)
		}

		on<SummaryInternalEvent.ProfilePictureRemovalStarted> { state, _ ->
			state.copy(isProfilePictureLoading = true)
		}

		on<SummaryInternalEvent.ProfilePictureRemovalSucceeded>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(
				profilePictureUrl = "",
				isProfilePictureLoading = false
			)
		}

		on<SummaryInternalEvent.ProfilePictureRemovalFailed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(
				profilePictureUrl = if (event.clearProfilePicture) "" else state.profilePictureUrl,
				isProfilePictureLoading = false
			)
		}
	}
}
