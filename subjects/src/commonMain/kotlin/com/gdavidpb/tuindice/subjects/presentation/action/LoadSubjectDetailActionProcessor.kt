package com.gdavidpb.tuindice.subjects.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailLoad
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.mapper.toViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class LoadSubjectDetailActionProcessor(
	private val loadSubjectDetailUseCase: LoadSubjectDetailUseCase
) : ActionProcessor<
	SubjectDetail.State,
	SubjectDetail.Action.LoadSubjectDetail,
	SubjectDetail.Effect
	> {
	override suspend fun process(
		action: SubjectDetail.Action.LoadSubjectDetail,
		sideEffect: (SubjectDetail.Effect) -> Unit
	): Flow<Mutation<SubjectDetail.State>> {
		return loadSubjectDetailUseCase.execute(SubjectDetailParams(subjectCode = action.subjectCode))
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> when (val load = useCaseState.value) {
						SubjectDetailLoad.LoadingRemote ->
							suspend { _: SubjectDetail.State ->
								SubjectDetail.State.Loading
							}

						is SubjectDetailLoad.Data ->
							suspend { _: SubjectDetail.State ->
								load.result.toViewState()
							}
						}

					is UseCaseState.Error ->
						suspend { _: SubjectDetail.State ->
							SubjectDetail.State.Failed(subjectCode = action.subjectCode)
						}
				}
			}
	}
}
