package com.gdavidpb.tuindice.pensum.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.InitialContentRefreshGate
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase.Result
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SetPensumSummaryCollapsedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumSelectionParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.pensum.presentation.transition.contentTransitions
import com.gdavidpb.tuindice.pensum.presentation.transition.emptyTransitions
import com.gdavidpb.tuindice.pensum.presentation.transition.idleTransitions
import com.gdavidpb.tuindice.pensum.presentation.transition.recordDataUnavailableTransitions
import kotlinx.coroutines.flow.Flow

class PensumMachine(
	private val observePensumUseCase: ObservePensumUseCase,
	private val ensurePensumLoadedUseCase: EnsurePensumLoadedUseCase,
	private val updatePensumUseCase: UpdatePensumUseCase,
	private val selectPensumUseCase: SelectPensumUseCase,
	private val selectPensumModalityUseCase: SelectPensumModalityUseCase,
	private val selectPensumSelectionUseCase: SelectPensumSelectionUseCase,
	private val setPensumSummaryCollapsedUseCase: SetPensumSummaryCollapsedUseCase
) : ScreenMachine<Pensum.State, Pensum.Effect> {
	override fun initialState(): Pensum.State = Pensum.State.Idle

	override fun define(host: MachineHost<Pensum.Effect>): MachineDefinition<Pensum.State> {
		return MachineDefinition.define {
			idleTransitions(machine = this@PensumMachine, host = host)
			contentTransitions()
			emptyTransitions()
			recordDataUnavailableTransitions()
			anyStateTransitions(machine = this@PensumMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<Pensum.Effect>) {
		host.launchMachineJob {
			observePensumUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						useCaseState.value.toInternalEvent()
					)

					is UseCaseState.Error -> host.processInternalEvent(
						PensumInternalEvent.PensumObservationFailed
					)
				}
			}
		}
	}

	internal fun refreshPensum(host: MachineHost<Pensum.Effect>) {
		launchRefresh(host = host, results = updatePensumUseCase.execute(Unit))
	}

	internal fun ensurePensumLoaded(host: MachineHost<Pensum.Effect>) {
		host.launchMachineJob {
			val initialRefreshGate = InitialContentRefreshGate(
				isCached = { result: Result -> result == Result.Cached },
				isRefreshStarted = { result -> result == Result.RefreshStarted }
			)

			ensurePensumLoadedUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val result = useCaseState.value
						if (initialRefreshGate.shouldProcess(result)) {
							when (result) {
								Result.Cached -> Unit
								Result.RefreshStarted -> host.processInternalEvent(
									PensumInternalEvent.PensumRefreshLoading
								)

								Result.RefreshSucceeded -> host.processInternalEvent(
									PensumInternalEvent.PensumRefreshSucceeded
								)
							}
						}
					}

					is UseCaseState.Error -> {
						if (initialRefreshGate.shouldProcessError()) {
							host.processInternalEvent(
								useCaseState.error.toRefreshFailureEvent(
									isSelectionChange = false
								)
							)
						}
					}
				}
			}
		}
	}

	internal fun selectPensum(host: MachineHost<Pensum.Effect>, year: Int) {
		launchRefresh(
			host = host,
			results = selectPensumUseCase.execute(SelectPensumParams(year = year)),
			isSelectionChange = true
		)
	}

	internal fun selectModality(host: MachineHost<Pensum.Effect>, modalityId: String) {
		launchRefresh(
			host = host,
			results = selectPensumModalityUseCase.execute(modalityId),
			isSelectionChange = true
		)
	}

	internal fun selectSelection(
		host: MachineHost<Pensum.Effect>,
		year: Int,
		modalityId: String
	) {
		launchRefresh(
			host = host,
			results = selectPensumSelectionUseCase.execute(
				SelectPensumSelectionParams(
					year = year,
					modalityId = modalityId
				)
			),
			isSelectionChange = true
		)
	}

	internal fun setSummaryCollapsed(host: MachineHost<Pensum.Effect>, isCollapsed: Boolean) {
		host.launchMachineJob {
			setPensumSummaryCollapsedUseCase.execute(isCollapsed).collect {
				// State is updated optimistically by the transition; the settings flow confirms it.
			}
		}
	}

	private fun launchRefresh(
		host: MachineHost<Pensum.Effect>,
		results: Flow<UseCaseState<*, UpdatePensumUseCaseError>>,
		isSelectionChange: Boolean = false
	) {
		host.launchMachineJob {
			results.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						PensumInternalEvent.PensumRefreshLoading
					)

					is UseCaseState.Data -> host.processInternalEvent(
						PensumInternalEvent.PensumRefreshSucceeded
					)

					is UseCaseState.Error -> host.processInternalEvent(
						useCaseState.error.toRefreshFailureEvent(
							isSelectionChange = isSelectionChange
						)
					)
				}
			}
		}
	}

	private fun UpdatePensumUseCaseError?.toRefreshFailureEvent(
		isSelectionChange: Boolean
	): PensumInternalEvent {
		return if (this == UpdatePensumUseCaseError.NotFound) {
			PensumInternalEvent.PensumRefreshNotFound
		} else {
			PensumInternalEvent.PensumRefreshFailed(
				error = this,
				isSelectionChange = isSelectionChange
			)
		}
	}

	private fun PensumObservation.toInternalEvent(): PensumInternalEvent {
		return when (this) {
			is PensumObservation.Content ->
				PensumInternalEvent.PensumContentObserved(
					pensum = pensum,
					isSummaryCollapsed = isSummaryCollapsed
				)

			PensumObservation.Missing,
			PensumObservation.WaitingForRecordData ->
				PensumInternalEvent.PensumDataMissing

			PensumObservation.RecordDataUnavailable ->
				PensumInternalEvent.PensumRecordDataUnavailableObserved
		}
	}
}
