package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.mapper.profilePictureIdentity

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
			// A changed picture identity or version means the pending picture mutation
			// landed: only then the in-flight flag and the optimistic preview are
			// released. The identity strips the signed-URL signature, which rotates on
			// every backend fetch without the picture itself changing.
			val hasNewProfilePicture =
				profilePictureIdentity(event.content.profilePictureUrl) !=
					profilePictureIdentity(state.profilePictureUrl) ||
					event.content.profilePictureVersion != state.profilePictureVersion

			event.content.copy(
				isProfilePictureLoading = if (hasNewProfilePicture) {
					false
				} else {
					state.isProfilePictureLoading
				},
				profilePictureLocalPreview = if (hasNewProfilePicture) {
					null
				} else {
					state.profilePictureLocalPreview
				},
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

		on<SummaryInternalEvent.ProfilePictureUploadStarted> { state, event ->
			state.copy(
				isProfilePictureLoading = true,
				profilePictureLocalPreview = event.previewPath
			)
		}

		on<SummaryInternalEvent.ProfilePictureUploadSucceeded>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			// The preview and the in-flight flag survive the upload ack on purpose;
			// UserObserved releases them once the new picture identity is local.
			state
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
				profilePictureLocalPreview = null,
				isProfilePictureLoading = false
			)
		}

		on<SummaryInternalEvent.ProfilePictureRemovalFailed>(
			emits = setOf(Summary.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Summary.Effect.ShowSnackBar(message = event.message))

			state.copy(
				profilePictureUrl = if (event.clearProfilePicture) "" else state.profilePictureUrl,
				profilePictureLocalPreview = null,
				isProfilePictureLoading = false
			)
		}
	}
}
