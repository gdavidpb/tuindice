package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TakeProfilePictureActionProcessor(
	private val takeProfilePictureUseCase: TakeProfilePictureUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Summary.State, Summary.Action.TakeProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.TakeProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return takeProfilePictureUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						state
					}

					is UseCaseState.Data -> { state ->
						sideEffect(
							Summary.Effect.OpenCamera(output = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> { state ->
						sideEffect(
							Summary.Effect.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_default_error)
							)
						)

						state
					}
				}
			}
	}
}