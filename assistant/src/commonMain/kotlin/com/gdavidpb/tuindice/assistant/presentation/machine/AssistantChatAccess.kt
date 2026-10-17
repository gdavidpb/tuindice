package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.AcceptAssistantConsentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.AppealAssistantRestrictionUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantStatusUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

// The assistant's standing with the student: whether it is on, the consent, and the restriction and its appeal.
class AssistantChatAccess(
	private val availabilityRepository: AssistantAvailabilityRepository,
	private val consentRepository: AssistantConsentRepository,
	private val acceptAssistantConsentUseCase: AcceptAssistantConsentUseCase,
	private val getAssistantStatusUseCase: GetAssistantStatusUseCase,
	private val appealAssistantRestrictionUseCase: AppealAssistantRestrictionUseCase,
	private val feedbackTexts: AssistantChatFeedbackTexts
) {
	private var availabilityJob: Job? = null

	fun currentAvailability(): AssistantAvailability = availabilityRepository.currentAvailability()

	fun isConsentGranted(): Boolean = consentRepository.assistantConsentGranted.value

	// Started once per sheet, before the opening reads the current value: a change between the two is not lost.
	fun observeAvailability(host: MachineHost<AssistantChat.Effect>) {
		if (availabilityJob?.isActive == true) return

		availabilityJob = host.launchMachineJob {
			availabilityRepository.observeAvailability().collect { availability ->
				host.processInternalEvent(availability.toInternalEvent())
			}
		}
	}

	fun acceptConsent(host: MachineHost<AssistantChat.Effect>) {
		host.launchMachineJob {
			acceptAssistantConsentUseCase.execute(true).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(AssistantChatInternalEvent.ConsentAccepted)
				}
			}
		}
	}

	// The standing says whether the restriction can be appealed; without it the appeal stays off.
	fun restrictionStatus(host: MachineHost<AssistantChat.Effect>) {
		host.launchMachineJob {
			getAssistantStatusUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantChatInternalEvent.RestrictionChecked(standing = useCaseState.value.standing)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantChatInternalEvent.RestrictionChecked(standing = null)
					)
				}
			}
		}
	}

	fun appeal(host: MachineHost<AssistantChat.Effect>, text: String) {
		host.launchMachineJob {
			appealAssistantRestrictionUseCase.execute(text).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantChatInternalEvent.AppealSent(message = feedbackTexts.appealSent())
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantChatInternalEvent.AppealRejected(
							message = feedbackTexts.appealFailed(AbuseAppeal.MAX_LENGTH)
						)
					)
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
