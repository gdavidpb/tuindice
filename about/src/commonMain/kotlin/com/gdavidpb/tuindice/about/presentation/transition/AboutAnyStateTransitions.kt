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
			About.State.Content(versionText = event.versionText)
		}

		// A failed version read must not leave the screen blank: fall back to Content
		// so the license, the developer and the credits stay reachable.
		onTo<AboutInternalEvent.AboutVersionLoadFailed, About.State.Content> { _, event ->
			About.State.Content(versionText = event.versionFallbackText)
		}

		on<About.Action.OpenUrl> { state, action ->
			machine.openExternalUrl(host = host, url = action.url)
			state
		}

		on<About.Action.ContactDeveloper> { state, _ ->
			machine.contactDeveloper(host = host)
			state
		}

		on<AboutInternalEvent.ContactEmailUriLoaded>(
			emits = setOf(About.Effect.OpenUri::class)
		) { state, event ->
			host.sendEffect(About.Effect.OpenUri(uri = event.uri))
			state
		}
	}
}
