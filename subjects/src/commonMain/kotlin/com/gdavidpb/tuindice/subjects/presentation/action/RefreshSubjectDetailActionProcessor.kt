package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.mapper.toViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RefreshSubjectDetailActionProcessor(
	private val refreshSubjectDetailUseCase: RefreshSubjectDetailUseCase
) : ActionProcessor<
	SubjectDetail.State,
	SubjectDetail.Action.RefreshSubjectDetail,
	SubjectDetail.Effect
	>() {
	override suspend fun process(
		action: SubjectDetail.Action.RefreshSubjectDetail,
		sideEffect: (SubjectDetail.Effect) -> Unit
	): Flow<Mutation<SubjectDetail.State>> {
		return refreshSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						suspend { state: SubjectDetail.State ->
							when (state) {
								is SubjectDetail.State.Content -> state
								is SubjectDetail.State.Unavailable -> state
								is SubjectDetail.State.Failed,
								SubjectDetail.State.Idle,
								SubjectDetail.State.Loading,
								-> SubjectDetail.State.Loading
							}
						}

					is UseCaseState.Data ->
						suspend { _: SubjectDetail.State ->
							useCaseState.value.toViewState()
						}

					is UseCaseState.Error ->
						suspend { state: SubjectDetail.State ->
							when (state) {
								is SubjectDetail.State.Content -> state
								is SubjectDetail.State.Unavailable -> state
								is SubjectDetail.State.Failed,
								SubjectDetail.State.Idle,
								SubjectDetail.State.Loading,
								-> SubjectDetail.State.Failed(subjectCode = action.subjectCode)
							}
						}
				}
			}
	}
}
