package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CreateSyntheticTermActionProcessor(
	private val createSyntheticTermUseCase: CreateSyntheticTermUseCase,
	private val updateSyntheticTermUseCase: UpdateSyntheticTermUseCase
) : ActionProcessor<
	CreateSyntheticTerm.State,
	CreateSyntheticTerm.Action.CreateTerm,
	CreateSyntheticTerm.Effect
	>() {
	override suspend fun process(
		action: CreateSyntheticTerm.Action.CreateTerm,
		sideEffect: (CreateSyntheticTerm.Effect) -> Unit
	): Flow<Mutation<CreateSyntheticTerm.State>> {
		return flow {
			val params = CreateSyntheticTermParams(
				editingTermId = action.editingTermId,
				editingTermKey = action.editingTermKey,
				period = action.period,
				subjects = action.subjects
			)
			val result = if (action.editingTermId == null) {
				createSyntheticTermUseCase.execute(params)
			} else {
				updateSyntheticTermUseCase.execute(params)
			}

			result.collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(isSubmitting = true)
							}
						)

					is UseCaseState.Data -> {
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(isSubmitting = false)
							}
						)
						sideEffect(CreateSyntheticTerm.Effect.NavigateBack)
					}

					is UseCaseState.Error ->
						emit(
							suspend { state: CreateSyntheticTerm.State ->
								state.copy(isSubmitting = false)
							}
						)
				}
			}
		}
	}
}
