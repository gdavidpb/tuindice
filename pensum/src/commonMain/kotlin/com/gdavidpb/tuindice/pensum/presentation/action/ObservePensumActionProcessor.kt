package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.toScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.snack_default_error

class ObservePensumActionProcessor(
	private val observePensumUseCase: ObservePensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.ObservePensum, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.ObservePensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return observePensumUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> suspend { state: Pensum.State ->
						when (val observation = useCaseState.value) {
								is PensumObservation.Content ->
									Pensum.State.Content(model = observation.pensum.toScreenModel())

								PensumObservation.Missing,
								PensumObservation.WaitingForRecordData,
								-> when (state) {
									Pensum.State.Empty -> Pensum.State.Empty
									Pensum.State.Idle,
									is Pensum.State.Content,
									Pensum.State.Failed,
									Pensum.State.Loading,
								-> Pensum.State.Loading
							}

								PensumObservation.RecordDataUnavailable -> when (state) {
									Pensum.State.Empty -> Pensum.State.Empty
									Pensum.State.Idle,
									is Pensum.State.Content,
									Pensum.State.Failed,
									Pensum.State.Loading,
								-> Pensum.State.Failed
							}
						}
					}

					is UseCaseState.Error -> suspend { _: Pensum.State ->
						sideEffect(Pensum.Effect.ShowSnackBar(getString(Res.string.snack_default_error)))
						Pensum.State.Failed
					}
				}
			}
	}
}
