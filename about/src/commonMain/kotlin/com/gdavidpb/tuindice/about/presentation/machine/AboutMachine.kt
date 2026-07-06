package com.gdavidpb.tuindice.about.presentation.machine

import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
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
import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
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
	private val usageDataConsentRepository: UsageDataConsentRepository
) : ScreenMachine<About.State, About.Effect> {
	override fun initialState(): About.State = About.State.Idle

	override fun define(host: MachineHost<About.Effect>): MachineDefinition<About.State> {
		return MachineDefinition.define {
			aboutContentTransitions(machine = this@AboutMachine)
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
}
