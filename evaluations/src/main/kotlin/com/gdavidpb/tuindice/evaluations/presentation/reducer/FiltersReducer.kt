package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FiltersReducer
	: BaseReducer<Evaluations.State, Evaluations.Event, List<EvaluationFilter>, Nothing>() {
	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<List<EvaluationFilter>, Nothing>
	): Flow<ViewOutput> {
		val filters = useCaseState.value

		return flow {
			emit(
				Evaluations.Event.ShowFilterEvaluationsDialog(filters)
			)
		}
	}
}