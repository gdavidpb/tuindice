package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultAboutTextProvider
import com.gdavidpb.tuindice.about.presentation.transition.aboutAnyStateTransitions
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import kotlinx.coroutines.flow.collect

class AboutMachine(
	private val loadVersionUseCase: LoadVersionUseCase,
	private val externalUseCases: AboutExternalUseCases,
	private val textProvider: AboutTextProvider = DefaultAboutTextProvider()
) : ScreenMachine<About.State, About.Effect> {
	override fun initialState(): About.State = About.State.Idle

	override fun define(host: MachineHost<About.Effect>): MachineDefinition<About.State> {
		return MachineDefinition.define {
			aboutAnyStateTransitions(machine = this@AboutMachine, host = host)
		}
	}

	internal fun loadVersion(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			loadVersionUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AboutInternalEvent.AboutVersionLoaded(versionText = useCaseState.value)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AboutInternalEvent.AboutVersionLoadFailed(
							versionFallbackText = textProvider.versionUnavailable()
						)
					)
				}
			}
		}
	}

	internal fun contactDeveloper(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			externalUseCases.sendSupportEmail.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AboutInternalEvent.ContactEmailUriLoaded(uri = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun openExternalUrl(host: MachineHost<About.Effect>, url: String) {
		host.launchMachineJob {
			externalUseCases.openExternalUrl.execute(url).collect()
		}
	}
}
