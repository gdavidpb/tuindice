package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toUpdateSubjectParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpdateSubjectActionProcessor(
	private val updateSubjectUseCase: UpdateSubjectUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Record.State, Record.Action.UpdateSubject, Record.Effect>() {

	override fun process(
		action: Record.Action.UpdateSubject,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return updateSubjectUseCase.execute(params = action.toUpdateSubjectParams())
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
								message = resourceResolver.getString(R.string.snack_default_error)
							)
						)

						state
					}
				}
			}
	}
}