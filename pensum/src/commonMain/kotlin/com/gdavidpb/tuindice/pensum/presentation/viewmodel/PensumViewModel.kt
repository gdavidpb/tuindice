package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumSelectionParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent
import com.gdavidpb.tuindice.pensum.presentation.mapper.toFailedMessage
import com.gdavidpb.tuindice.pensum.presentation.mapper.toLocalDataWarningMessage
import com.gdavidpb.tuindice.pensum.presentation.mapper.toScreenModel
import kotlinx.coroutines.flow.Flow
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message

class PensumViewModel(
	private val observePensumUseCase: ObservePensumUseCase,
	private val updatePensumUseCase: UpdatePensumUseCase,
	private val selectPensumUseCase: SelectPensumUseCase,
	private val selectPensumModalityUseCase: SelectPensumModalityUseCase,
	private val selectPensumSelectionUseCase: SelectPensumSelectionUseCase,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Pensum.State, Pensum.Action, Pensum.Effect>(
	name = "pensum",
	initialState = Pensum.State.Idle,
	initialAction = Pensum.Action.ObservePensum,
	dispatchers = dispatchers
) {
	fun refreshPensumAction() {
		sendAction(Pensum.Action.RefreshPensum)
	}

	fun selectPensumAction(year: Int) {
		sendAction(Pensum.Action.SelectPensum(year = year))
	}

	fun selectModalityAction(modalityId: String) {
		sendAction(Pensum.Action.SelectModality(modalityId = modalityId))
	}

	fun selectSelectionAction(year: Int, modalityId: String) {
		sendAction(
			Pensum.Action.SelectSelection(
				year = year,
				modalityId = modalityId
			)
		)
	}

	override fun defineMachine() = MachineDefinition.define<Pensum.State> {
		from<Pensum.State.Idle> {
			on<Pensum.Action.ObservePensum> { state, _ ->
				startObservation()
				state
			}
		}

		from<Pensum.State.Content> {
			on<PensumInternalEvent.PensumContentObserved> { state, event ->
				state.copy(model = event.pensum.toScreenModel())
			}

			on<PensumInternalEvent.PensumRefreshLoading> { state, _ ->
				state.copy(isRefreshing = true, localDataMessage = null)
			}

			on<PensumInternalEvent.PensumRefreshFailed> { state, event ->
				state.copy(
					isRefreshing = false,
					localDataMessage = event.error.toLocalDataWarningMessage()
				)
			}
		}

		from<Pensum.State.Empty> {
			on<PensumInternalEvent.PensumDataMissing> { state, _ -> state }

			on<PensumInternalEvent.PensumRecordDataUnavailableObserved> { state, _ -> state }
		}

		from<Pensum.State.RecordDataUnavailable> {
			on<PensumInternalEvent.PensumDataMissing> { state, _ -> state }

			on<PensumInternalEvent.PensumRecordDataUnavailableObserved> { state, _ -> state }
		}

		fromAny {
			on<Pensum.Action.RefreshPensum> { state, _ ->
				launchRefresh(results = updatePensumUseCase.execute(Unit))
				state
			}

			on<Pensum.Action.SelectPensum> { state, action ->
				launchRefresh(
					results = selectPensumUseCase.execute(
						SelectPensumParams(year = action.year)
					)
				)
				state
			}

			on<Pensum.Action.SelectModality> { state, action ->
				launchRefresh(results = selectPensumModalityUseCase.execute(action.modalityId))
				state
			}

			on<Pensum.Action.SelectSelection> { state, action ->
				launchRefresh(
					results = selectPensumSelectionUseCase.execute(
						SelectPensumSelectionParams(
							year = action.year,
							modalityId = action.modalityId
						)
					)
				)
				state
			}

			onTo<PensumInternalEvent.PensumContentObserved, Pensum.State.Content> { _, event ->
				Pensum.State.Content(model = event.pensum.toScreenModel())
			}

			onTo<PensumInternalEvent.PensumDataMissing, Pensum.State.Loading> { _, _ ->
				Pensum.State.Loading
			}

			onTo<PensumInternalEvent.PensumRecordDataUnavailableObserved, Pensum.State.RecordDataUnavailable> { _, _ ->
				Pensum.State.RecordDataUnavailable
			}

			onTo<PensumInternalEvent.PensumObservationFailed, Pensum.State.Failed> { _, _ ->
				Pensum.State.Failed(
					message = UiText.Resource(Res.string.pensum_failed_message)
				)
			}

			onTo<PensumInternalEvent.PensumRefreshLoading, Pensum.State.Loading> { _, _ ->
				Pensum.State.Loading
			}

			on<PensumInternalEvent.PensumRefreshSucceeded> { state, _ ->
				when (state) {
					is Pensum.State.Content ->
						state.copy(isRefreshing = false, localDataMessage = null)

					else -> state
				}
			}

			onTo<PensumInternalEvent.PensumRefreshNotFound, Pensum.State.Empty> { _, _ ->
				Pensum.State.Empty
			}

			onTo<PensumInternalEvent.PensumRefreshFailed, Pensum.State.Failed> { _, event ->
				Pensum.State.Failed(message = event.error.toFailedMessage())
			}
		}
	}

	private fun startObservation() {
		launchMachineJob {
			observePensumUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> processInternalEvent(
						useCaseState.value.toInternalEvent()
					)

					is UseCaseState.Error -> processInternalEvent(
						PensumInternalEvent.PensumObservationFailed
					)
				}
			}
		}
	}

	private fun launchRefresh(results: Flow<UseCaseState<*, UpdatePensumUseCaseError>>) {
		launchMachineJob {
			results.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> processInternalEvent(
						PensumInternalEvent.PensumRefreshLoading
					)

					is UseCaseState.Data -> processInternalEvent(
						PensumInternalEvent.PensumRefreshSucceeded
					)

					is UseCaseState.Error -> processInternalEvent(
						if (useCaseState.error == UpdatePensumUseCaseError.NotFound)
							PensumInternalEvent.PensumRefreshNotFound
						else
							PensumInternalEvent.PensumRefreshFailed(error = useCaseState.error)
					)
				}
			}
		}
	}

	private fun PensumObservation.toInternalEvent(): PensumInternalEvent {
		return when (this) {
			is PensumObservation.Content ->
				PensumInternalEvent.PensumContentObserved(pensum = pensum)

			PensumObservation.Missing,
			PensumObservation.WaitingForRecordData ->
				PensumInternalEvent.PensumDataMissing

			PensumObservation.RecordDataUnavailable ->
				PensumInternalEvent.PensumRecordDataUnavailableObserved
		}
	}
}
