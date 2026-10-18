package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.AcceptAssistantConsentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SyncAssistantUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withTimeoutOrNull

// The assistant's standing with the student: whether it is on, the consent and the privacy policy it links, and in
// [restrictions] the restriction and its appeal.
class AssistantChatAccess(
	private val availabilityRepository: AssistantAvailabilityRepository,
	private val consentRepository: AssistantConsentRepository,
	private val acceptAssistantConsentUseCase: AcceptAssistantConsentUseCase,
	private val syncAssistantUseCase: SyncAssistantUseCase,
	private val appEnvironmentRepository: AppEnvironmentRepository,
	internal val restrictions: AssistantChatRestrictions
) {
	private var availabilityJob: Job? = null

	suspend fun firstAvailability(): AssistantAvailability = availabilityRepository.observeAvailability().first()

	fun isConsentGranted(): Boolean = consentRepository.assistantConsentGranted.value

	// The privacy policy of the environment the app runs in, the same page About and the sign-in open, under [title]. It
	// opens in the app's browser, over the chat: the sheet stays where it was, and back returns to it.
	fun openPrivacyPolicy(host: MachineHost<AssistantChat.Effect>, title: String) {
		host.sendEffect(
			AssistantChat.Effect.NavigateToBrowser(
				title = title,
				url = appEnvironmentRepository.getEnvironment().privacyPolicyUrl
			)
		)
	}

	// Started once per sheet, after the opening read the first value; the flow replays the current one, so no change
	// in between is lost.
	fun observeAvailability(host: MachineHost<AssistantChat.Effect>) {
		if (availabilityJob?.isActive == true) return

		availabilityJob = host.launchMachineJob {
			availabilityRepository.observeAvailability().collect { availability ->
				host.processInternalEvent(availability.toInternalEvent())
			}
		}
	}

	// Reads the status the server gives the student and applies it: its mode becomes the last word on the mode, and the
	// consent the device keeps follows the server's. A status that cannot be read leaves both as they are.
	suspend fun applyStatus(): AssistantStatus? {
		return (syncAssistantUseCase.execute(Unit).last() as? UseCaseState.Data)?.value
	}

	// The status is read before the sheet decides what it shows, but not for longer than [millis]: a status that is late
	// leaves the sheet to the values the device keeps, and null.
	suspend fun applyStatusWithin(millis: Long): AssistantStatus? {
		return withTimeoutOrNull(millis) { applyStatus() }
	}

	// The status read again after a refused guided send. An on or guided mode reaches the sheet even when the mode did not
	// change, since an unchanged mode does not reach it through the availability.
	fun refreshStatus(host: MachineHost<AssistantChat.Effect>) {
		host.launchMachineJob {
			val status = applyStatus() ?: return@launchMachineJob
			val mode = AssistantMode.fromRemoteValue(status.mode)

			if (mode != AssistantMode.OFF) {
				host.processInternalEvent(AssistantChatInternalEvent.AssistantOn(guided = mode == AssistantMode.GUIDED))
			}
		}
	}

	fun acceptConsent(host: MachineHost<AssistantChat.Effect>) {
		host.launchMachineJob {
			acceptAssistantConsentUseCase.execute(true).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(AssistantChatInternalEvent.ConsentAccepted)

					is UseCaseState.Error -> host.processInternalEvent(AssistantChatInternalEvent.ConsentAcceptFailed)
				}
			}
		}
	}

	private fun AssistantAvailability.toInternalEvent(): AssistantChatInternalEvent {
		return if (isVisible) {
			AssistantChatInternalEvent.AssistantOn(guided = mode == AssistantMode.GUIDED)
		} else {
			AssistantChatInternalEvent.AssistantOff
		}
	}
}
