package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.presentation.reducer.ViewOutput
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class SummaryReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Summary.State, Summary.Event, Account, GetAccountError>() {

	override fun reduceUnrecoverableState(
		currentState: Summary.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Summary.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<Account, GetAccountError>
	): Flow<ViewOutput> {
		return flowOf(Summary.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<Account, GetAccountError>
	): Flow<ViewOutput> {
		val summaryViewState = useCaseState.value.toSummaryViewState(resourceResolver)

		return flowOf(Summary.State.Loaded(value = summaryViewState))
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<Account, GetAccountError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is GetAccountError.AccountDisabled ->
					emit(Summary.Event.NavigateToAccountDisabled)

				is GetAccountError.NoConnection ->
					emit(Summary.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

				is GetAccountError.OutdatedPassword ->
					emit(Summary.Event.NavigateToOutdatedPassword)

				is GetAccountError.Timeout ->
					emit(Summary.Event.ShowTimeoutSnackBar)

				is GetAccountError.Unavailable -> {
					emit(Summary.Event.ShowTryLaterSnackBar)

					if (currentState is Summary.State.Loaded) {
						val newState = currentState.value.copy(
							isUpdated = false
						)

						emit(Summary.State.Loaded(newState))

						return@flow
					}
				}

				else ->
					emit(Summary.Event.ShowDefaultErrorSnackBar)
			}

			emit(Summary.State.Failed)
		}
	}
}