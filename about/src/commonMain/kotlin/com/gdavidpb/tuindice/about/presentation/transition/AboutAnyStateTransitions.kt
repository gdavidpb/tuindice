package com.gdavidpb.tuindice.about.presentation.transition

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

internal fun MachineDefinitionBuilder<About.State>.aboutAnyStateTransitions(
	machine: AboutMachine,
	host: MachineHost<About.Effect>
) {
	fromAny {
		on<About.Action.LoadVersion> { state, _ ->
			machine.loadVersion(host = host)
			state
		}

		onTo<AboutInternalEvent.AboutVersionLoaded, About.State.Content> { _, event ->
			// The observation starts with the content it feeds. LoadVersion is the initial
			// action, so this runs once per screen.
			machine.startAssistantObservation(host = host)

			About.State.Content(
				versionText = event.versionText,
				usageDataCollectionEnabled = event.usageDataCollectionEnabled
			)
		}

		// A failed version read must not leave the screen blank: fall back to Content
		// so the support and legal entry points stay reachable.
		onTo<AboutInternalEvent.AboutVersionLoadFailed, About.State.Content> { _, event ->
			machine.startAssistantObservation(host = host)

			About.State.Content(
				versionText = event.versionFallbackText,
				usageDataCollectionEnabled = event.usageDataCollectionEnabled
			)
		}

		on<About.Action.OpenTermsAndConditions>(
			emits = setOf(About.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openTermsAndConditions(host = host)
			state
		}

		on<About.Action.OpenPrivacyPolicy>(
			emits = setOf(About.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openPrivacyPolicy(host = host)
			state
		}

		on<About.Action.OpenSupport>(
			emits = setOf(About.Effect.NavigateToBrowser::class)
		) { state, _ ->
			machine.openSupport(host = host)
			state
		}

		on<About.Action.ShareApp>(
			emits = setOf(About.Effect.ShareText::class)
		) { state, _ ->
			machine.shareApp(host = host)
			state
		}

		on<About.Action.OpenAssistantMemory>(
			emits = setOf(About.Effect.NavigateToAssistantMemory::class)
		) { state, _ ->
			host.sendEffect(About.Effect.NavigateToAssistantMemory)
			state
		}

		// The row only asks: the confirmation is a destination of its own, and it answers with the deletion.
		on<About.Action.RequestDeleteAssistantConversations>(
			emits = setOf(About.Effect.NavigateToDeleteAssistantConversationsConfirmation::class)
		) { state, _ ->
			host.sendEffect(About.Effect.NavigateToDeleteAssistantConversationsConfirmation)
			state
		}

		on<About.Action.ConfirmDeleteAssistantConversations> { state, _ ->
			machine.deleteAssistantConversations(host = host)
			state
		}

		on<AboutInternalEvent.AssistantConversationsDeleted>(
			emits = setOf(About.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(About.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<AboutInternalEvent.AssistantConversationsDeleteFailed>(
			emits = setOf(About.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(About.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<About.Action.OpenUrl> { state, action ->
			machine.openExternalUrl(host = host, url = action.url)
			state
		}

		on<About.Action.RateOnStore> { state, _ ->
			machine.openStore(host = host)
			state
		}

		on<About.Action.ReportBug> { state, _ ->
			machine.contactSupport(host = host)
			state
		}

		on<About.Action.ContactDeveloper> { state, _ ->
			machine.contactSupport(host = host)
			state
		}

		on<AboutInternalEvent.SupportEmailUriLoaded>(
			emits = setOf(About.Effect.OpenUri::class)
		) { state, event ->
			host.sendEffect(About.Effect.OpenUri(uri = event.uri))
			state
		}

		on<AboutInternalEvent.StoreUriLoaded>(
			emits = setOf(About.Effect.OpenUri::class)
		) { state, event ->
			host.sendEffect(About.Effect.OpenUri(uri = event.uri))
			state
		}
	}
}
