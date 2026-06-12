package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailLoad
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.mapper.toViewState
import com.gdavidpb.tuindice.subjects.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.subjects.presentation.transition.contentTransitions
import com.gdavidpb.tuindice.subjects.presentation.transition.unavailableTransitions

class SubjectDetailMachine(
	private val loadSubjectDetailUseCase: LoadSubjectDetailUseCase,
	private val refreshSubjectDetailUseCase: RefreshSubjectDetailUseCase
) {
	fun define(host: MachineHost<SubjectDetail.Effect>): MachineDefinition<SubjectDetail.State> {
		return MachineDefinition.define {
			contentTransitions()
			unavailableTransitions()
			anyStateTransitions(machine = this@SubjectDetailMachine, host = host)
		}
	}

	internal fun startLoad(
		host: MachineHost<SubjectDetail.Effect>,
		action: SubjectDetail.Action.LoadSubjectDetail
	) {
		host.launchMachineJob {
			loadSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
				.collect { useCaseState ->
					when (useCaseState) {
						is UseCaseState.Loading -> Unit

						is UseCaseState.Data -> when (val load = useCaseState.value) {
							SubjectDetailLoad.LoadingRemote -> host.processInternalEvent(
								SubjectDetailInternalEvent.DetailLoadStarted
							)

							is SubjectDetailLoad.Data -> emitLoaded(
								host = host,
								result = load.result
							)
						}

						is UseCaseState.Error -> host.processInternalEvent(
							SubjectDetailInternalEvent.DetailLoadFailed(
								subjectCode = action.subjectCode
							)
						)
					}
				}
		}
	}

	internal fun startRefresh(
		host: MachineHost<SubjectDetail.Effect>,
		action: SubjectDetail.Action.RefreshSubjectDetail
	) {
		host.launchMachineJob {
			refreshSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
				.collect { useCaseState ->
					when (useCaseState) {
						is UseCaseState.Loading -> host.processInternalEvent(
							SubjectDetailInternalEvent.DetailRefreshStarted
						)

						is UseCaseState.Data -> emitLoaded(
							host = host,
							result = useCaseState.value
						)

						is UseCaseState.Error -> host.processInternalEvent(
							SubjectDetailInternalEvent.DetailRefreshFailed(
								subjectCode = action.subjectCode
							)
						)
					}
				}
		}
	}

	private suspend fun emitLoaded(
		host: MachineHost<SubjectDetail.Effect>,
		result: SubjectDetailResult
	) {
		when (val viewState = result.toViewState()) {
			is SubjectDetail.State.Content -> host.processInternalEvent(
				SubjectDetailInternalEvent.DetailContentLoaded(detail = viewState.detail)
			)

			is SubjectDetail.State.Unavailable -> host.processInternalEvent(
				SubjectDetailInternalEvent.DetailUnavailableLoaded(
					subjectCode = viewState.subjectCode
				)
			)

			else -> Unit
		}
	}
}
