package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.usecase.AppealAssistantRestrictionUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SyncAssistantUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseAppeal
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import kotlinx.coroutines.flow.collect
import kotlin.time.Clock

// The restriction the server puts on the student and its appeal. The end the standing gives is weighed against
// [clock] whenever the chat is drawn or a send starts, never on a timer: the standing is read again when the sheet
// opens and when a send follows an end that has passed.
class AssistantChatRestrictions(
	private val syncAssistantUseCase: SyncAssistantUseCase,
	private val appealAssistantRestrictionUseCase: AppealAssistantRestrictionUseCase,
	private val feedbackTexts: AssistantChatFeedbackTexts,
	internal val clock: Clock
) {
	fun nowMillis(): Long = clock.now().toEpochMilliseconds()

	// The restriction a standing read now holds: a pause, a suspension or a ban whose end has not passed. A warning, no
	// level, or an end already past is no restriction, so the chat is released.
	internal fun restrictionOf(standing: AbuseStanding): AssistantChatSession.Restriction? {
		if (standing.level !in RESTRICTING_LEVELS) return null

		val restriction = AssistantChatSession.Restriction(
			level = standing.level,
			untilMillis = standing.untilMillis,
			canAppeal = standing.appealable
		)

		return restriction.takeUnless { held -> held.isOver(nowMillis()) }
	}

	// The standing says what the restriction is, until when, and whether it can be appealed; without it the appeal stays
	// off and the end unknown.
	fun restrictionStatus(host: MachineHost<AssistantChat.Effect>) {
		host.launchMachineJob {
			syncAssistantUseCase.execute(Unit).collect { useCaseState ->
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
}

// The levels that stop the student from writing: the server refuses their turns while they hold.
private val RESTRICTING_LEVELS = setOf(StandingLevel.COOLDOWN, StandingLevel.SUSPENDED, StandingLevel.BANNED)
