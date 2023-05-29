package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class RemoveProfilePictureReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Summary.State, Summary.Event, Unit, ProfilePictureError>() {

	override fun reduceUnrecoverableState(
		currentState: Summary.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Summary.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<Unit, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			if (currentState is Summary.State.Content) {
				val newState = currentState.copy(
					isProfilePictureLoading = true
				)

				emit(newState)
			}
		}
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<Unit, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			emit(
				Summary.Event.ShowSnackBar(
					message = resourceResolver.getString(R.string.snack_profile_picture_removed)
				)
			)

			if (currentState is Summary.State.Content) {
				val newState = currentState.copy(
					profilePictureUrl = "",
					isProfilePictureLoading = false
				)

				emit(newState)
			}
		}
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<Unit, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is ProfilePictureError.Timeout ->
					emit(
						Summary.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_timeout)
						)
					)

				is ProfilePictureError.NoConnection ->
					emit(
						Summary.Event.ShowSnackBar(
							message = if (error.isNetworkAvailable)
								resourceResolver.getString(R.string.snack_service_unavailable)
							else
								resourceResolver.getString(R.string.snack_network_unavailable)
						)
					)

				else ->
					emit(
						Summary.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}

			if (currentState is Summary.State.Content) {
				val newState = currentState.copy(
					isProfilePictureLoading = false
				)

				emit(newState)
			}
		}
	}
}