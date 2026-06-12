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
			About.State.Content(
				versionText = event.versionText,
				usageDataCollectionEnabled = event.usageDataCollectionEnabled
			)
		}

		onTo<AboutInternalEvent.AboutVersionLoadFailed, About.State.Idle> { _, _ ->
			About.State.Idle
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
