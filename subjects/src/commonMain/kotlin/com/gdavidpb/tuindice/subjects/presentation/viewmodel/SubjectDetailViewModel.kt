package com.gdavidpb.tuindice.subjects.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailLoad
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailInternalEvent
import com.gdavidpb.tuindice.subjects.presentation.mapper.toViewState
import com.gdavidpb.tuindice.subjects.presentation.mapper.withSelectedTab

class SubjectDetailViewModel(
	private val loadSubjectDetailUseCase: LoadSubjectDetailUseCase,
	private val refreshSubjectDetailUseCase: RefreshSubjectDetailUseCase,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SubjectDetail.State, SubjectDetail.Action, SubjectDetail.Effect>(
	name = "subject_detail",
	initialState = SubjectDetail.State.Idle,
	dispatchers = dispatchers
) {
	fun loadSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.LoadSubjectDetail(subjectCode = subjectCode))
	}

	fun refreshSubjectDetailAction(subjectCode: String) {
		sendAction(SubjectDetail.Action.RefreshSubjectDetail(subjectCode = subjectCode))
	}

	fun selectSubjectSegmentTabAction(tab: SubjectSegmentTab) {
		sendAction(SubjectDetail.Action.SelectSubjectSegmentTab(tab = tab))
	}

	override fun defineMachine() = MachineDefinition.define<SubjectDetail.State> {
		from<SubjectDetail.State.Content> {
			on<SubjectDetail.Action.SelectSubjectSegmentTab> { state, action ->
				state.copy(detail = state.detail.withSelectedTab(action.tab))
			}

			on<SubjectDetailInternalEvent.DetailRefreshStarted> { state, _ -> state }

			on<SubjectDetailInternalEvent.DetailRefreshFailed> { state, _ -> state }
		}

		from<SubjectDetail.State.Unavailable> {
			on<SubjectDetailInternalEvent.DetailRefreshStarted> { state, _ -> state }

			on<SubjectDetailInternalEvent.DetailRefreshFailed> { state, _ -> state }
		}

		fromAny {
			on<SubjectDetail.Action.LoadSubjectDetail> { state, action ->
				startLoad(action = action)
				state
			}

			on<SubjectDetail.Action.RefreshSubjectDetail> { state, action ->
				startRefresh(action = action)
				state
			}

			onTo<SubjectDetailInternalEvent.DetailLoadStarted, SubjectDetail.State.Loading> { _, _ ->
				SubjectDetail.State.Loading
			}

			onTo<SubjectDetailInternalEvent.DetailRefreshStarted, SubjectDetail.State.Loading> { _, _ ->
				SubjectDetail.State.Loading
			}

			onTo<SubjectDetailInternalEvent.DetailContentLoaded, SubjectDetail.State.Content> { _, event ->
				SubjectDetail.State.Content(detail = event.detail)
			}

			onTo<SubjectDetailInternalEvent.DetailUnavailableLoaded, SubjectDetail.State.Unavailable> { _, event ->
				SubjectDetail.State.Unavailable(subjectCode = event.subjectCode)
			}

			onTo<SubjectDetailInternalEvent.DetailLoadFailed, SubjectDetail.State.Failed> { _, event ->
				SubjectDetail.State.Failed(subjectCode = event.subjectCode)
			}

			onTo<SubjectDetailInternalEvent.DetailRefreshFailed, SubjectDetail.State.Failed> { _, event ->
				SubjectDetail.State.Failed(subjectCode = event.subjectCode)
			}
		}
	}

	private fun startLoad(action: SubjectDetail.Action.LoadSubjectDetail) {
		launchMachineJob {
			loadSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
				.collect { useCaseState ->
					when (useCaseState) {
						is UseCaseState.Loading -> Unit

						is UseCaseState.Data -> when (val load = useCaseState.value) {
							SubjectDetailLoad.LoadingRemote -> processInternalEvent(
								SubjectDetailInternalEvent.DetailLoadStarted
							)

							is SubjectDetailLoad.Data -> emitLoaded(result = load.result)
						}

						is UseCaseState.Error -> processInternalEvent(
							SubjectDetailInternalEvent.DetailLoadFailed(
								subjectCode = action.subjectCode
							)
						)
					}
				}
		}
	}

	private fun startRefresh(action: SubjectDetail.Action.RefreshSubjectDetail) {
		launchMachineJob {
			refreshSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
				.collect { useCaseState ->
					when (useCaseState) {
						is UseCaseState.Loading -> processInternalEvent(
							SubjectDetailInternalEvent.DetailRefreshStarted
						)

						is UseCaseState.Data -> emitLoaded(result = useCaseState.value)

						is UseCaseState.Error -> processInternalEvent(
							SubjectDetailInternalEvent.DetailRefreshFailed(
								subjectCode = action.subjectCode
							)
						)
					}
				}
		}
	}

	private suspend fun emitLoaded(result: SubjectDetailResult) {
		when (val viewState = result.toViewState()) {
			is SubjectDetail.State.Content -> processInternalEvent(
				SubjectDetailInternalEvent.DetailContentLoaded(detail = viewState.detail)
			)

			is SubjectDetail.State.Unavailable -> processInternalEvent(
				SubjectDetailInternalEvent.DetailUnavailableLoaded(
					subjectCode = viewState.subjectCode
				)
			)

			else -> Unit
		}
	}
}
