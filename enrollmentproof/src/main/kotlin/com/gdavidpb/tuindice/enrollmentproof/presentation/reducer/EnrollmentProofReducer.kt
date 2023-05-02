package com.gdavidpb.tuindice.enrollmentproof.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.presentation.reducer.ViewOutput
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class EnrollmentProofReducer :
	BaseReducer<Enrollment.State, Enrollment.Event, String, FetchEnrollmentProofError>() {

	override suspend fun reduceLoadingState(
		currentState: Enrollment.State,
		useCaseState: UseCaseState.Loading<String, FetchEnrollmentProofError>
	): Flow<ViewOutput> {
		return flowOf(Enrollment.State.Fetching)
	}

	override suspend fun reduceDataState(
		currentState: Enrollment.State,
		useCaseState: UseCaseState.Data<String, FetchEnrollmentProofError>
	): Flow<ViewOutput> {
		return flow {
			emit(Enrollment.Event.OpenEnrollmentProof(path = useCaseState.value))
			emit(Enrollment.Event.CloseDialog)

			emit(Enrollment.State.Fetched)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Enrollment.State,
		useCaseState: UseCaseState.Error<String, FetchEnrollmentProofError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is FetchEnrollmentProofError.AccountDisabled ->
					emit(Enrollment.Event.NavigateToAccountDisabled)

				is FetchEnrollmentProofError.NoConnection ->
					emit(Enrollment.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

				is FetchEnrollmentProofError.NotFound ->
					emit(Enrollment.Event.ShowNotFoundSnackBar)

				is FetchEnrollmentProofError.UnsupportedFile ->
					emit(Enrollment.Event.ShowUnsupportedFileSnackBar)

				is FetchEnrollmentProofError.OutdatedPassword ->
					emit(Enrollment.Event.NavigateToOutdatedPassword)

				is FetchEnrollmentProofError.Timeout ->
					emit(Enrollment.Event.ShowTimeoutSnackBar)

				is FetchEnrollmentProofError.Unavailable ->
					emit(Enrollment.Event.ShowUnavailableSnackBar)

				else ->
					emit(Enrollment.Event.ShowDefaultErrorError)
			}

			emit(Enrollment.Event.CloseDialog)
			emit(Enrollment.State.Failed)
		}
	}
}