package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toStep1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LoadAddEvaluationStep1Reducer
	: BaseReducer<AddEvaluation.State, AddEvaluation.Event, List<Subject>, Nothing>() {
	override suspend fun reduceLoadingState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Loading<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		val currentStep = if (currentState is AddEvaluation.State.Step2)
			currentState.toStep1()
		else
			AddEvaluation.State.Step1()

		return flowOf(AddEvaluation.State.Loading(state = currentStep))
	}

	override suspend fun reduceDataState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Data<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		val currentStep = currentState.toStep1()

		return flowOf(
			AddEvaluation.State.Step1(
				availableSubjects = useCaseState.value,
				subject = currentStep?.subject,
				type = currentStep?.type
			)
		)
	}

	override suspend fun reduceErrorState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Error<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return if (currentState is AddEvaluation.State.Loading)
			flowOf(AddEvaluation.State.Failed(state = currentState.state))
		else
			super.reduceErrorState(currentState, useCaseState)
	}
}