package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class EvaluationsReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Evaluations.State, Evaluations.Event, GetEvaluations, EvaluationsError>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluations.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Loading<GetEvaluations, EvaluationsError>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<GetEvaluations, EvaluationsError>
	): Flow<ViewOutput> {
		val evaluations = useCaseState.value

		return flow {
			if (evaluations.originalEvaluations.isNotEmpty())
				emit(
					Evaluations.State.Content(
						originalEvaluations = evaluations.originalEvaluations,
						filteredEvaluations = evaluations.filteredEvaluations,
						availableFilters = evaluations.availableFilters,
						activeFilters = evaluations.activeFilters
					)
				)
			else
				emit(
					Evaluations.State.Empty
				)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<GetEvaluations, EvaluationsError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is EvaluationsError.NoConnection ->
					emit(
						Evaluations.Event.ShowSnackBar(
							message = if (error.isNetworkAvailable)
								resourceResolver.getString(R.string.snack_service_unavailable)
							else
								resourceResolver.getString(R.string.snack_network_unavailable)
						)
					)

				is EvaluationsError.Timeout ->
					emit(
						Evaluations.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_timeout)
						)
					)

				is EvaluationsError.Unavailable ->
					emit(
						Evaluations.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_service_unavailable)
						)
					)

				else ->
					emit(
						Evaluations.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}

			emit(Evaluations.State.Failed)
		}
	}
}