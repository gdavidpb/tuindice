package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toSetSubjectGradeParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error

class SetSubjectGradeActionProcessor(
	private val setSubjectGradeUseCase: SetSubjectGradeUseCase
) : ActionProcessor<Record.State, Record.Action.SetSubjectGrade, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.SetSubjectGrade,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setSubjectGradeUseCase.execute(params = action.toSetSubjectGradeParams())
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						state
					}

					is UseCaseState.Data -> suspend { state ->
						state
					}

					is UseCaseState.Error -> suspend { state: Record.State ->
						val message = getString(Res.string.snack_default_error)

						sideEffect(
							Record.Effect.ShowSnackBar(
								message = message
							)
						)

						state
					}
				}
			}
	}
}
