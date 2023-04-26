package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryViewState

class SummaryReducer(
	override val useCase: GetAccountUseCase,
	private val resourceResolver: ResourceResolver
) : BaseReducer<Unit, Account, GetAccountError, Summary.State, Summary.Action.LoadSummary, Summary.Event>() {
	override fun actionToParams(action: Summary.Action.LoadSummary) {}

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<Account, GetAccountError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State {
		return Summary.State.Loading
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<Account, GetAccountError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State {
		val summaryViewState = useCaseState.value.toSummaryViewState(resourceResolver)

		return Summary.State.Loaded(value = summaryViewState)
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<Account, GetAccountError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State {
		when (val error = useCaseState.error) {
			is GetAccountError.AccountDisabled ->
				eventProducer(Summary.Event.NavigateToAccountDisabled)

			is GetAccountError.NoConnection ->
				eventProducer(Summary.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

			is GetAccountError.OutdatedPassword ->
				eventProducer(Summary.Event.NavigateToOutdatedPassword)

			is GetAccountError.Timeout ->
				eventProducer(Summary.Event.ShowTimeoutSnackBar)

			is GetAccountError.Unavailable -> {
				eventProducer(Summary.Event.ShowTryLaterSnackBar)

				if (currentState is Summary.State.Loaded) {
					val newState = currentState.value.copy(
						isUpdated = false
					)

					return Summary.State.Loaded(newState)
				}
			}

			else ->
				eventProducer(Summary.Event.ShowDefaultErrorSnackBar)
		}

		return Summary.State.Failed
	}
}