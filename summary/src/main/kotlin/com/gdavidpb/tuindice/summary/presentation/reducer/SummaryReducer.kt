package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
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
		return flowOf(
			with(useCaseState.value) {
				Summary.State.Content(
					name = toShortName(),
					lastUpdate = resourceResolver.getString(
						R.string.text_last_update,
						lastUpdate.formatLastUpdate()
					),
					careerName = careerName,
					grade = grade.toFloat(),
					enrolledSubjects = enrolledSubjects,
					enrolledCredits = enrolledCredits,
					approvedSubjects = approvedSubjects,
					approvedCredits = approvedCredits,
					retiredSubjects = retiredSubjects,
					retiredCredits = retiredCredits,
					failedSubjects = failedSubjects,
					failedCredits = failedCredits,
					profilePictureUrl = pictureUrl,
					isGradeVisible = (grade > 0.0),
					isProfilePictureLoading = false,
					isLoading = false,
					isUpdated = true,
					isUpdating = false
				)
			}
		)
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<Account, GetAccountError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is GetAccountError.NoConnection ->
					emit(
						Summary.Event.ShowSnackBar(
							message = if (error.isNetworkAvailable)
								resourceResolver.getString(R.string.snack_service_unavailable)
							else
								resourceResolver.getString(R.string.snack_network_unavailable)
						)
					)

				is GetAccountError.OutdatedPassword -> {
					if (currentState is Summary.State.Content) {
						val newState = currentState.copy(
							isUpdating = false
							// TODO dialog = SummaryDialog.OutdatedPassword
						)

						emit(newState)

						return@flow
					}
				}

				is GetAccountError.Timeout ->
					emit(
						Summary.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_timeout)
						)
					)

				is GetAccountError.Unavailable -> {
					emit(
						Summary.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_no_service)
						)
					)

					if (currentState is Summary.State.Content) {
						val newState = currentState.copy(
							isUpdated = false
						)

						emit(newState)

						return@flow
					}
				}

				else ->
					emit(
						Summary.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}

			emit(Summary.State.Failed)
		}
	}
}