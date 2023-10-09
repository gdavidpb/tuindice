package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddEvaluationStep1Reducer :
	BaseReducer<AddEvaluation.State, AddEvaluation.Event, Unit, EvaluationError>() {
	override suspend fun reduceErrorState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Error<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return super.reduceErrorState(currentState, useCaseState)
	}

	override suspend fun reduceDataState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Data<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return flow {
			if (currentState is AddEvaluation.State.Step1) {
				emit(
					AddEvaluation.State.Step2(
						subject = currentState.subject!!,
						type = currentState.type!!
					)
				)

				emit(
					AddEvaluation.Event.NavigateTo(
						subRoute = Destination.AddEvaluation.Step2
					)
				)
			}
		}
	}
}