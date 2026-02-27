package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toSetSubjectGradeParams
import com.gdavidpb.tuindice.record.presentation.resource.RecordTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetSubjectGradeActionProcessor(
	private val setSubjectGradeUseCase: SetSubjectGradeUseCase,
	private val textProvider: RecordTextProvider
) : ActionProcessor<Record.State, Record.Action.SetSubjectGrade, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.SetSubjectGrade,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setSubjectGradeUseCase.execute(params = action.toSetSubjectGradeParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						state
					}

					is UseCaseState.Error -> { state ->
						sideEffect(
							Record.Effect.ShowSnackBar(
								message = textProvider.defaultError()
							)
						)

						state
					}
				}
			}
	}
}
