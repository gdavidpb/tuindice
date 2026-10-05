package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentLoadResult
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.SyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.model.resolveSyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.statemachine.InitialContentRefreshGate
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.EnsureRecordLoadedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveNewStudentNoRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermRejectionsUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.recordRejectionMessage
import com.gdavidpb.tuindice.record.presentation.mapper.resolveRecordNotice
import com.gdavidpb.tuindice.record.presentation.mapper.toRecordFailureMessage
import com.gdavidpb.tuindice.record.presentation.transition.recordAnyStateTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordContentTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordEmptyTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordFailedTransitions
import com.gdavidpb.tuindice.record.presentation.transition.recordIdleTransitions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_synthetic_term_delete_failed
import tuindice.record.generated.resources.snack_synthetic_term_deleted

class RecordMachine(
	private val observeRecordUseCase: ObserveRecordUseCase,
	private val observeNewStudentNoRecordUseCase: ObserveNewStudentNoRecordUseCase,
	private val observeSyntheticTermRejectionsUseCase: ObserveSyntheticTermRejectionsUseCase,
	private val ensureRecordLoadedUseCase: EnsureRecordLoadedUseCase,
	private val updateRecordUseCase: UpdateRecordUseCase,
	private val setRecordViewModeUseCase: SetRecordViewModeUseCase,
	private val setSelectedTermUseCase: SetSelectedTermUseCase,
	private val upsertAttemptSelectionUseCase: UpsertAttemptSelectionUseCase,
	private val deleteSyntheticTermUseCase: DeleteSyntheticTermUseCase
) : ScreenMachine<Record.State, Record.Effect> {
	// ObserveRecord is an Idle self-loop, and launchMachineJob accumulates rather than
	// replaces: a second dispatch would leave two observers acknowledging and announcing
	// the same rejection twice. Today only the initial action dispatches it, so these
	// handles keep that guarantee from depending on the call site.
	private var recordObservationJob: Job? = null
	private var syntheticTermRejectionObservationJob: Job? = null
	private var newStudentObservationJob: Job? = null

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
		startSideObservations(host = host)

		if (recordObservationJob?.isActive == true) return

		recordObservationJob = host.launchMachineJob {
			observeRecordUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val record = useCaseState.value
						val notice = resolveRecordNotice(record)

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
									selectedTermId = requireNotNull(record.selectedTermId),
									notice = notice
								)
							)

							SyncedContentResolution.Empty -> host.processInternalEvent(
								RecordInternalEvent.RecordEmptyObserved(notice = notice)
							)

							SyncedContentResolution.Loading,
							SyncedContentResolution.KeepCurrent,
							-> host.processInternalEvent(
								RecordInternalEvent.RecordWaitingObserved(
									// Read from the observation that says the record is still to come,
									// so the wait and its reason are one fact.
									isNewStudentNoRecord =
										record.syncStatus == SyncStatus.NewStudentNoRecord
								)
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

	// What the screen listens to besides the record itself: whether the account is a new student
	// with no record yet, and the edits the server refused for good. The first has its own
	// observation because the record's never emits for an account with nothing stored.
	private fun startSideObservations(host: MachineHost<Record.Effect>) {
		if (newStudentObservationJob?.isActive != true) {
			newStudentObservationJob = host.launchMachineJob {
				observeNewStudentNoRecordUseCase.execute(Unit).collect { useCaseState ->
					if (useCaseState is UseCaseState.Data) {
						host.processInternalEvent(
							RecordInternalEvent.NewStudentNoRecordObserved(
								isNewStudentNoRecord = useCaseState.value
							)
						)
					}
				}
			}
		}

		if (syntheticTermRejectionObservationJob?.isActive == true) return

		syntheticTermRejectionObservationJob = host.launchMachineJob {
			observeSyntheticTermRejectionsUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					// A grade and a term are rejected for different reasons, so each gets its own snackbar.
					useCaseState.value.groupBy(RecordRejection::kind).forEach { (kind, rejections) ->
						host.processInternalEvent(
							RecordInternalEvent.SyntheticTermRejected(
								message = recordRejectionMessage(kind = kind, count = rejections.size)
							)
						)
					}
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
							message = useCaseState.error.toRecordFailureMessage(),
							navigateToOutdatedCredentials =
								useCaseState.error == RecordUseCaseError.Unauthorized,
							isNewStudentNoRecord = observeNewStudentNoRecordUseCase.currentValue()
						)
					)
				}
			}
		}
	}

	internal fun ensureRecordLoaded(host: MachineHost<Record.Effect>) {
		host.launchMachineJob {
			val initialRefreshGate = InitialContentRefreshGate(
				isCached = { result: InitialContentLoadResult ->
					result == InitialContentLoadResult.Cached
				},
				isRefreshStarted = { result ->
					result == InitialContentLoadResult.RefreshStarted
				}
			)

			ensureRecordLoadedUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val result = useCaseState.value
						if (initialRefreshGate.shouldProcess(result)) {
							when (result) {
								InitialContentLoadResult.Cached -> Unit
								InitialContentLoadResult.RefreshStarted -> host.processInternalEvent(
									RecordInternalEvent.RecordRefreshStarted
								)

								InitialContentLoadResult.RefreshSucceeded -> Unit
							}
						}
					}

					is UseCaseState.Error -> {
						if (initialRefreshGate.shouldProcessError()) {
							host.processInternalEvent(
								RecordInternalEvent.RecordRefreshFailed(
									message = useCaseState.error.toRecordFailureMessage(),
									navigateToOutdatedCredentials =
										useCaseState.error == RecordUseCaseError.Unauthorized,
									isNewStudentNoRecord = observeNewStudentNoRecordUseCase.currentValue()
								)
							)
						}
					}
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
		outcome: AttemptOutcome?
	) {
		host.launchMachineJob {
			upsertAttemptSelectionUseCase.execute(
				UpsertAttemptSelectionParams(
					attemptId = attemptId,
					grade = grade,
					outcome = outcome
				)
			).collect { useCaseState ->
				if (useCaseState is UseCaseState.Error) {
					if (useCaseState.error == RecordUseCaseError.Unauthorized) {
						host.processInternalEvent(RecordInternalEvent.RecordUnauthorized)
					} else {
						host.processInternalEvent(
							RecordInternalEvent.AttemptSelectionFailed(
								message = useCaseState.error.toRecordFailureMessage()
							)
						)
					}
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

// Whether the university has no record for this account yet, as of now. A refresh asks it in its
// own job at the moment it fails, so the failure re-enters the table already knowing why; a sync
// that learns it later reaches Failed as NewStudentNoRecordObserved. Between the two the table
// needs no register that one job writes for another to read.
private suspend fun ObserveNewStudentNoRecordUseCase.currentValue(): Boolean {
	return execute(Unit)
		.mapNotNull { useCaseState -> (useCaseState as? UseCaseState.Data)?.value }
		.firstOrNull()
		?: false
}
