package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadProfilePictureReducer :
	BaseReducer<Summary.State, Summary.Event, String, ProfilePictureError>() {

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<String, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			if (currentState is Summary.State.Loaded) {
				val newState = currentState.value.copy(
					isProfilePictureLoading = true
				)

				emit(Summary.State.Loaded(newState))
			}
		}
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<String, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			emit(Summary.Event.ShowProfilePictureUpdatedSnackBar)

			if (currentState is Summary.State.Loaded) {
				val newState = currentState.value.copy(
					profilePictureUrl = useCaseState.value,
					isProfilePictureLoading = false
				)

				emit(Summary.State.Loaded(newState))
			}
		}
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<String, ProfilePictureError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is ProfilePictureError.Timeout ->
					emit(Summary.Event.ShowTimeoutSnackBar)

				is ProfilePictureError.NoConnection ->
					emit(Summary.Event.ShowNoConnectionSnackBar(error.isNetworkAvailable))

				else ->
					emit(Summary.Event.ShowDefaultErrorSnackBar)
			}

			if (currentState is Summary.State.Loaded) {
				val newState = currentState.value.copy(
					isProfilePictureLoading = false
				)

				emit(Summary.State.Loaded(newState))
			}
		}
	}
}