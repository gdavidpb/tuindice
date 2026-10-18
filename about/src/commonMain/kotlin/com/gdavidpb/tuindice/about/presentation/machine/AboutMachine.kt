package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.AssistantConsentSetting
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.transition.aboutAnyStateTransitions
import com.gdavidpb.tuindice.about.presentation.transition.aboutContentTransitions
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_assistant_consent_failed
import tuindice.about.generated.resources.about_assistant_conversations_delete_failed
import tuindice.about.generated.resources.about_assistant_conversations_deleted
import tuindice.about.generated.resources.about_share_message
import tuindice.about.generated.resources.about_share_subject
import tuindice.about.generated.resources.about_version_unavailable
import tuindice.about.generated.resources.label_privacy_policy
import tuindice.about.generated.resources.label_support
import tuindice.about.generated.resources.label_terms_and_conditions

class AboutMachine(
	private val loadVersionUseCase: LoadVersionUseCase,
	private val sendSupportEmailUseCase: SendSupportEmailUseCase,
	private val openStoreUseCase: OpenStoreUseCase,
	private val openExternalUrlUseCase: OpenExternalUrlUseCase,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val observeAssistantAvailabilityUseCase: ObserveAssistantAvailabilityUseCase,
	private val observeAssistantConsentUseCase: ObserveAssistantConsentUseCase,
	private val setAssistantConsentUseCase: SetAssistantConsentUseCase,
	private val deleteAssistantConversationsUseCase: DeleteAssistantConversationsUseCase
) : ScreenMachine<About.State, About.Effect> {
	override fun initialState(): About.State = About.State.Idle

	override fun define(host: MachineHost<About.Effect>): MachineDefinition<About.State> {
		return MachineDefinition.define {
			aboutContentTransitions(machine = this@AboutMachine, host = host)
			aboutAnyStateTransitions(machine = this@AboutMachine, host = host)
		}
	}

	internal fun loadVersion(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			loadVersionUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AboutInternalEvent.AboutVersionLoaded(
							versionText = useCaseState.value,
							usageDataCollectionEnabled =
								usageDataConsentRepository.isUsageDataCollectionEnabled()
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AboutInternalEvent.AboutVersionLoadFailed(
							versionFallbackText = getString(Res.string.about_version_unavailable),
							usageDataCollectionEnabled =
								usageDataConsentRepository.isUsageDataCollectionEnabled()
						)
					)
				}
			}
		}
	}

	internal fun contactSupport(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			sendSupportEmailUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AboutInternalEvent.SupportEmailUriLoaded(uri = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun openStore(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			openStoreUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AboutInternalEvent.StoreUriLoaded(uri = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun openExternalUrl(host: MachineHost<About.Effect>, url: String) {
		host.launchMachineJob {
			openExternalUrlUseCase.execute(url).collect()
		}
	}

	internal suspend fun openTermsAndConditions(host: MachineHost<About.Effect>) {
		host.sendEffect(
			About.Effect.NavigateToBrowser(
				title = getString(Res.string.label_terms_and_conditions),
				url = appEnvironmentRepository.getEnvironment().termsAndConditionsUrl
			)
		)
	}

	internal suspend fun openPrivacyPolicy(host: MachineHost<About.Effect>) {
		host.sendEffect(
			About.Effect.NavigateToBrowser(
				title = getString(Res.string.label_privacy_policy),
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)
	}

	internal suspend fun openSupport(host: MachineHost<About.Effect>) {
		host.sendEffect(
			About.Effect.NavigateToBrowser(
				title = getString(Res.string.label_support),
				url = appEnvironmentRepository.getEnvironment().supportUrl
			)
		)
	}

	internal suspend fun shareApp(host: MachineHost<About.Effect>) {
		host.sendEffect(
			About.Effect.ShareText(
				subject = getString(Res.string.about_share_subject),
				text = getString(Res.string.about_share_message)
			)
		)
	}

	internal fun persistUsageDataCollection(enabled: Boolean) {
		usageDataConsentRepository.setUsageDataCollectionEnabled(enabled)
	}

	// Started by the rows that create the content: the observed values only reach a
	// state that exists, and the first emission of each flow is the current value.
	internal fun startAssistantObservation(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			observeAssistantAvailabilityUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AboutInternalEvent.AssistantAvailabilityObserved(visible = useCaseState.value)
					)

					if (useCaseState.value) adoptServerAssistantConsent(host)
				}
			}
		}

		host.launchMachineJob {
			observeAssistantConsentUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AboutInternalEvent.AssistantConsentObserved(granted = useCaseState.value)
					)
				}
			}
		}
	}

	// The consent the device keeps is empty after a reinstall, a new device or a sign-out, while the server may hold it
	// accepted; the switch reads the device's, so the server's is adopted when the row shows. The answer reaches the
	// switch through the observation above, and a failed read leaves the switch as it is.
	private fun adoptServerAssistantConsent(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			setAssistantConsentUseCase.execute(AssistantConsentSetting.FromServer).collect()
		}
	}

	internal fun setAssistantConsent(host: MachineHost<About.Effect>, granted: Boolean) {
		host.launchMachineJob {
			setAssistantConsentUseCase.execute(AssistantConsentSetting.Chosen(granted)).collect { useCaseState ->
				if (useCaseState is UseCaseState.Error) {
					// The failed write left the store as it was, so the value to restore is
					// the one the store holds now, not the one the switch was moved to.
					val restoredGranted = observeAssistantConsentUseCase.execute(Unit)
						.mapNotNull { storedState -> (storedState as? UseCaseState.Data)?.value }
						.first()

					host.processInternalEvent(
						AboutInternalEvent.AssistantConsentWriteFailed(
							restoredGranted = restoredGranted,
							message = getString(Res.string.about_assistant_consent_failed)
						)
					)
				}
			}
		}
	}

	internal fun deleteAssistantConversations(host: MachineHost<About.Effect>) {
		host.launchMachineJob {
			deleteAssistantConversationsUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AboutInternalEvent.AssistantConversationsDeleted(
							message = getString(Res.string.about_assistant_conversations_deleted)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AboutInternalEvent.AssistantConversationsDeleteFailed(
							message = getString(Res.string.about_assistant_conversations_delete_failed)
						)
					)
				}
			}
		}
	}
}
