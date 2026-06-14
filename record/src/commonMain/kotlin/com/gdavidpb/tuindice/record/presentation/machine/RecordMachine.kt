package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.SyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.model.resolveSyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.transition.recordAnyStateTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordContentTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordEmptyTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordFailedTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordIdleTransitions
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_synthetic_term_delete_failed
import tuindice.record.generated.resources.snack_synthetic_term_deleted

class RecordMachine(
	private val observeRecordUseCase: ObserveRecordUseCase,
	private val updateRecordUseCase: UpdateRecordUseCase,
	private val setRecordViewModeUseCase: SetRecordViewModeUseCase,
	private val setSelectedTermUseCase: SetSelectedTermUseCase,
	private val upsertAttemptSelectionUseCase: UpsertAttemptSelectionUseCase,
	private val deleteSyntheticTermUseCase: DeleteSyntheticTermUseCase
) : ScreenMachine<Record.State, Record.Effect> {
	override fun initialState(): Record.State = Record.State.Idle

	override fun define(host: MachineHost<Record.Effect>): MachineDefinition<Record.State> {
		return MachineDefinition.define {
			recordIdleTransitions(machine = this@RecordMachine, host = host)
			recordContentTransitions(machine = this@RecordMachine, host = host)
			recordEmptyTransitions(host = host)
			recordFailedTransitions()
			recordAnyStateTransitions(machine = this@RecordMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<Record.Effect>) {
		host.launchMachineJob {
			observeRecordUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val record = useCaseState.value

						when (
							resolveSyncedContentResolution(
								hasContent = record.selectedTermId != null,
								hasSynced = record.hasSyncedRecord,
								keepCurrentWhileWaiting = false
							)
						) {
							SyncedContentResolution.Content -> host.processInternalEvent(
								RecordInternalEvent.RecordContentObserved(
									viewMode = record.viewMode,
									record = record.record,
									selectedTermId = requireNotNull(record.selectedTermId)
								)
							)

							SyncedContentResolution.Empty -> host.processInternalEvent(
								RecordInternalEvent.RecordEmptyObserved
							)

							SyncedContentResolution.Loading,
							SyncedContentResolution.KeepCurrent,
							-> host.processInternalEvent(
								RecordInternalEvent.RecordWaitingObserved
							)
						}
					}

					is UseCaseState.Error -> host.processInternalEvent(
						RecordInternalEvent.RecordObservationFailed
					)
				}
			}
		}
	}

	internal fun refreshRecord(host: MachineHost<Record.Effect>) {
		host.launchMachineJob {
			updateRecordUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						RecordInternalEvent.RecordRefreshStarted
					)

					is UseCaseState.Data -> Unit

					is UseCaseState.Error -> host.processInternalEvent(
						RecordInternalEvent.RecordRefreshFailed(
							navigateToOutdatedCredentials =
								useCaseState.error == RecordUseCaseError.Unauthorized
						)
					)
				}
			}
		}
	}

	internal fun setViewMode(host: MachineHost<Record.Effect>, viewMode: RecordViewMode) {
		host.launchMachineJob {
			setRecordViewModeUseCase.execute(viewMode).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> host.processInternalEvent(
						RecordInternalEvent.RecordViewModeSet(viewMode = viewMode)
					)

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> Unit
				}
			}
		}
	}

	internal fun selectTerm(
		host: MachineHost<Record.Effect>,
		termId: String,
		viewMode: RecordViewMode
	) {
		host.launchMachineJob {
			setSelectedTermUseCase.execute(
				SetSelectedTermParams(
					viewMode = viewMode,
					termId = termId
				)
			).collect()
		}
	}

	internal fun upsertAttemptSelection(
		host: MachineHost<Record.Effect>,
		attemptId: String,
		grade: Int?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) {
		host.launchMachineJob {
			upsertAttemptSelectionUseCase.execute(
				UpsertAttemptSelectionParams(
					attemptId = attemptId,
					grade = grade,
					outcome = outcome,
					commit = commit
				)
			).collect { useCaseState ->
				if (
					useCaseState is UseCaseState.Error &&
					useCaseState.error == RecordUseCaseError.Unauthorized
				) {
					host.processInternalEvent(RecordInternalEvent.RecordUnauthorized)
				}
			}
		}
	}

	internal fun deleteSyntheticTerm(host: MachineHost<Record.Effect>, termId: String) {
		host.launchMachineJob {
			deleteSyntheticTermUseCase.execute(termId).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						RecordInternalEvent.SyntheticTermDeleted(
							message = getString(Res.string.snack_synthetic_term_deleted)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						RecordInternalEvent.SyntheticTermDeleteFailed(
							message = getString(Res.string.snack_synthetic_term_delete_failed),
							navigateToOutdatedCredentials =
								useCaseState.error == RecordUseCaseError.Unauthorized
						)
					)
				}
			}
		}
	}
}
