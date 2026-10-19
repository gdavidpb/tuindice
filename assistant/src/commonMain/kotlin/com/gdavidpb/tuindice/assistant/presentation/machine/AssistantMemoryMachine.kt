package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.AnswerStillAppliesUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ClearAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DeleteAssistantMemoryItemUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.AnswerStillAppliesParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantMemoryTexts
import com.gdavidpb.tuindice.assistant.presentation.mapper.toMemoryEntryItems
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantMemoryContentTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantMemoryFailedTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantMemoryIdleTransitions
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantMemoryLoadingTransitions
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine

/**
 * The memory screen: its states, its transitions and the commands behind them. The list is asked of the server each
 * time it loads, so the machine keeps nothing of its own between loads.
 */
class AssistantMemoryMachine(
	private val getAssistantMemoryUseCase: GetAssistantMemoryUseCase,
	private val deleteAssistantMemoryItemUseCase: DeleteAssistantMemoryItemUseCase,
	private val clearAssistantMemoryUseCase: ClearAssistantMemoryUseCase,
	private val answerStillAppliesUseCase: AnswerStillAppliesUseCase,
	private val texts: AssistantMemoryTexts
) : ScreenMachine<AssistantMemory.State, AssistantMemory.Effect> {
	override fun initialState(): AssistantMemory.State = AssistantMemory.State.Idle

	override fun define(host: MachineHost<AssistantMemory.Effect>): MachineDefinition<AssistantMemory.State> {
		return MachineDefinition.define {
			assistantMemoryIdleTransitions(machine = this@AssistantMemoryMachine, host = host)
			assistantMemoryLoadingTransitions()
			assistantMemoryFailedTransitions(machine = this@AssistantMemoryMachine, host = host)
			assistantMemoryContentTransitions(machine = this@AssistantMemoryMachine, host = host)
		}
	}

	// Asks the server for the list. Only what is still in force is listed; an empty list is the empty state.
	internal fun load(host: MachineHost<AssistantMemory.Effect>) {
		host.launchMachineJob {
			getAssistantMemoryUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val entries = useCaseState.value.items.toMemoryEntryItems(texts = texts)

						host.processInternalEvent(
							if (entries.isEmpty()) {
								AssistantMemoryInternalEvent.NothingLoaded
							} else {
								AssistantMemoryInternalEvent.EntriesLoaded(entries = entries)
							}
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantMemoryInternalEvent.LoadFailed(message = texts.loadFailed())
					)
				}
			}
		}
	}

	// Deletes [memoryId]. [isLast] tells whether it is the only row listed; the row decides it from the content it
	// reads.
	internal fun delete(host: MachineHost<AssistantMemory.Effect>, memoryId: String, isLast: Boolean) {
		host.launchMachineJob {
			deleteAssistantMemoryItemUseCase.execute(memoryId).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						if (isLast) {
							AssistantMemoryInternalEvent.LastEntryDeleted(
								memoryId = memoryId,
								message = texts.deleted()
							)
						} else {
							AssistantMemoryInternalEvent.EntryDeleted(memoryId = memoryId, message = texts.deleted())
						}
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantMemoryInternalEvent.EntryDeleteFailed(message = texts.deleteFailed())
					)
				}
			}
		}
	}

	internal fun answerStillApplies(host: MachineHost<AssistantMemory.Effect>, memoryId: String, applies: Boolean) {
		host.launchMachineJob {
			val params = AnswerStillAppliesParams(memoryId = memoryId, applies = applies)

			answerStillAppliesUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(AssistantMemoryInternalEvent.StillAppliesAnswered)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantMemoryInternalEvent.StillAppliesFailed(message = texts.answerFailed())
					)
				}
			}
		}
	}

	// Deletes everything the assistant remembers. The confirmation came from the dialog before this command runs.
	internal fun clear(host: MachineHost<AssistantMemory.Effect>) {
		host.launchMachineJob {
			clearAssistantMemoryUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantMemoryInternalEvent.MemoryCleared(message = texts.cleared())
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantMemoryInternalEvent.ClearFailed(message = texts.deleteFailed())
					)
				}
			}
		}
	}
}
