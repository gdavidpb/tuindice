package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error

class TakeProfilePictureActionProcessor(
	private val takeProfilePictureUseCase: TakeProfilePictureUseCase
) : ActionProcessor<Summary.State, Summary.Action.TakeProfilePicture, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.TakeProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return takeProfilePictureUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						state
					}

					is UseCaseState.Data -> suspend { state ->
						sideEffect(
							Summary.Effect.OpenCamera(output = useCaseState.value)
						)

						state
					}

					is UseCaseState.Error -> suspend { state: Summary.State ->
						val message = getString(Res.string.snack_default_error)

						sideEffect(
							Summary.Effect.ShowSnackBar(
								message = message
							)
						)

						state
					}
				}
			}
	}
}
