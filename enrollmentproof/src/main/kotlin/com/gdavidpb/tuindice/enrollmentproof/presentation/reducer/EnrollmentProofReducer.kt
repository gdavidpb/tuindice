package com.gdavidpb.tuindice.enrollmentproof.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.enrollmentproof.R
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class EnrollmentProofReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Enrollment.State, Enrollment.Event, String, FetchEnrollmentProofError>() {

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
			emit(
				Enrollment.Event.OpenEnrollmentProof(path = useCaseState.value)
			)

			emit(
				Enrollment.Event.CloseDialog
			)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Enrollment.State,
		useCaseState: UseCaseState.Error<String, FetchEnrollmentProofError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is FetchEnrollmentProofError.NoConnection ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = if (error.isNetworkAvailable)
								resourceResolver.getString(R.string.snack_service_unavailable)
							else
								resourceResolver.getString(R.string.snack_network_unavailable)
						)
					)

				is FetchEnrollmentProofError.NotFound ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_enrollment_not_found)
						)
					)

				is FetchEnrollmentProofError.UnsupportedFile ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_enrollment_unsupported)
						)
					)

				is FetchEnrollmentProofError.OutdatedPassword ->
					emit(
						Enrollment.Event.NavigateToOutdatedPassword
					)

				is FetchEnrollmentProofError.Timeout ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_timeout)
						)
					)

				is FetchEnrollmentProofError.Unavailable ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_service_unavailable)
						)
					)

				else ->
					emit(
						Enrollment.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}

			emit(
				Enrollment.Event.CloseDialog
			)
		}
	}
}