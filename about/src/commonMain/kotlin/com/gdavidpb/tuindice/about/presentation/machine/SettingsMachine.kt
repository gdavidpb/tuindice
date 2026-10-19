package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.resource.DefaultSettingsTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.SettingsTextProvider
import com.gdavidpb.tuindice.about.presentation.transition.settingsAnyStateTransitions
import com.gdavidpb.tuindice.about.presentation.transition.settingsContentTransitions
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class SettingsMachine(
	private val contentUseCases: SettingsContentUseCases,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val externalUseCases: SettingsExternalUseCases,
	private val assistantUseCases: SettingsAssistantUseCases,
	private val textProvider: SettingsTextProvider = DefaultSettingsTextProvider()
) : ScreenMachine<Settings.State, Settings.Effect> {
	override fun initialState(): Settings.State = Settings.State.Idle

	override fun define(host: MachineHost<Settings.Effect>): MachineDefinition<Settings.State> {
		return MachineDefinition.define {
			settingsContentTransitions(machine = this@SettingsMachine, host = host)
			settingsAnyStateTransitions(machine = this@SettingsMachine, host = host)
		}
	}

	internal fun loadVersion(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			contentUseCases.loadVersion.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SettingsInternalEvent.SettingsVersionLoaded(
							versionText = useCaseState.value,
							usageDataCollectionEnabled =
								usageDataConsentRepository.isUsageDataCollectionEnabled()
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SettingsInternalEvent.SettingsVersionLoadFailed(
							versionFallbackText = textProvider.versionUnavailable(),
							usageDataCollectionEnabled =
								usageDataConsentRepository.isUsageDataCollectionEnabled()
						)
					)
				}
			}
		}
	}

	internal fun contactSupport(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			externalUseCases.sendSupportEmail.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SettingsInternalEvent.SupportEmailUriLoaded(uri = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun openStore(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			externalUseCases.openStore.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SettingsInternalEvent.StoreUriLoaded(uri = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun openExternalUrl(host: MachineHost<Settings.Effect>, url: String) {
		host.launchMachineJob {
			externalUseCases.openExternalUrl.execute(url).collect()
		}
	}

	internal suspend fun openTermsAndConditions(host: MachineHost<Settings.Effect>) {
		host.sendEffect(
			Settings.Effect.NavigateToBrowser(
				title = textProvider.termsAndConditionsLabel(),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)
	}

	internal suspend fun openPrivacyPolicy(host: MachineHost<Settings.Effect>) {
		host.sendEffect(
			Settings.Effect.NavigateToBrowser(
				title = textProvider.privacyPolicyLabel(),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)
	}

	internal suspend fun openSupport(host: MachineHost<Settings.Effect>) {
		host.sendEffect(
			Settings.Effect.NavigateToBrowser(
				title = textProvider.supportLabel(),
				url = appEnvironmentRepository.getEnvironment().supportUrl
			)
		)
	}

	internal suspend fun shareApp(host: MachineHost<Settings.Effect>) {
		host.sendEffect(
			Settings.Effect.ShareText(
				subject = textProvider.shareSubject(),
				text = textProvider.shareMessage()
			)
		)
	}

	internal fun persistUsageDataCollection(enabled: Boolean) {
		usageDataConsentRepository.setUsageDataCollectionEnabled(enabled)
	}

	// Started by the rows that create the content: the observed values only reach a state that exists, and the first
	// emission of each flow is the current value. The identity is the one the device already holds, so it makes no
	// request and the card fills in as soon as the content is up.
	internal fun startObservation(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			contentUseCases.observeProfileIdentity.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SettingsInternalEvent.ProfileIdentityObserved(identity = useCaseState.value)
					)
				}
			}
		}

		host.launchMachineJob {
			assistantUseCases.observeAvailability.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SettingsInternalEvent.AssistantAvailabilityObserved(visible = useCaseState.value)
					)

					if (useCaseState.value) adoptServerAssistantConsent(host)
				}
			}
		}

		host.launchMachineJob {
			assistantUseCases.observeConsent.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SettingsInternalEvent.AssistantConsentObserved(granted = useCaseState.value)
					)
				}
			}
		}
	}

	// The consent the device keeps is empty after a reinstall, a new device or a sign-out, while the server may hold it
	// accepted; the switch reads the device's, so the server's is adopted when the row shows. The answer reaches the
	// switch through the observation above, and a failed read leaves the switch as it is.
	private fun adoptServerAssistantConsent(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			assistantUseCases.refreshConsent.execute(Unit).collect()
		}
	}

	internal fun setAssistantConsent(host: MachineHost<Settings.Effect>, granted: Boolean) {
		host.launchMachineJob {
			assistantUseCases.setConsent.execute(granted).collect { useCaseState ->
				if (useCaseState is UseCaseState.Error) {
					// The failed write left the store as it was, so the value to restore is
					// the one the store holds now, not the one the switch was moved to.
					val restoredGranted = assistantUseCases.observeConsent.execute(Unit)
						.mapNotNull { storedState -> (storedState as? UseCaseState.Data)?.value }
						.first()

					host.processInternalEvent(
						SettingsInternalEvent.AssistantConsentWriteFailed(
							restoredGranted = restoredGranted,
							message = textProvider.assistantConsentFailed()
						)
					)
				}
			}
		}
	}

	internal fun deleteAssistantConversations(host: MachineHost<Settings.Effect>) {
		host.launchMachineJob {
			assistantUseCases.deleteConversations.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SettingsInternalEvent.AssistantConversationsDeleted(
							message = textProvider.assistantConversationsDeleted()
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SettingsInternalEvent.AssistantConversationsDeleteFailed(
							message = textProvider.assistantConversationsDeleteFailed()
						)
					)
				}
			}
		}
	}
}
