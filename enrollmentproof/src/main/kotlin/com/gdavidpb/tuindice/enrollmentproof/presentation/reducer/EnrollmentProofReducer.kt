package com.gdavidpb.tuindice.enrollmentproof.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

class EnrollmentProofReducer(
	override val useCase: FetchEnrollmentProofUseCase
) : BaseReducer<Unit, String, FetchEnrollmentProofError, Enrollment.State, Enrollment.Action.FetchEnrollmentProof, Enrollment.Event>() {

	override fun actionToParams(action: Enrollment.Action.FetchEnrollmentProof) {}

	override suspend fun reduceLoadingState(
		state: UseCaseState.Loading<String, FetchEnrollmentProofError>,
		eventProducer: (Enrollment.Event) -> Unit
	): Enrollment.State {
		return Enrollment.State.Fetching
	}

	override suspend fun reduceDataState(
		state: UseCaseState.Data<String, FetchEnrollmentProofError>,
		eventProducer: (Enrollment.Event) -> Unit
	): Enrollment.State {
		eventProducer(Enrollment.Event.OpenEnrollmentProof(path = state.value))
		eventProducer(Enrollment.Event.CloseDialog)

		return Enrollment.State.Fetched
	}

	override suspend fun reduceErrorState(
		state: UseCaseState.Error<String, FetchEnrollmentProofError>,
		eventProducer: (Enrollment.Event) -> Unit
	): Enrollment.State {
		when (val error = state.error) {
			is FetchEnrollmentProofError.AccountDisabled ->
				eventProducer(Enrollment.Event.NavigateToAccountDisabled)

			is FetchEnrollmentProofError.NoConnection ->
				eventProducer(Enrollment.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

			is FetchEnrollmentProofError.NotFound ->
				eventProducer(Enrollment.Event.ShowNotFoundSnackBar)

			is FetchEnrollmentProofError.UnsupportedFile ->
				eventProducer(Enrollment.Event.ShowUnsupportedFileSnackBar)

			is FetchEnrollmentProofError.OutdatedPassword ->
				eventProducer(Enrollment.Event.NavigateToOutdatedPassword)

			is FetchEnrollmentProofError.Timeout ->
				eventProducer(Enrollment.Event.ShowTimeoutSnackBar)

			is FetchEnrollmentProofError.Unavailable ->
				eventProducer(Enrollment.Event.ShowUnavailableSnackBar)

			else ->
				eventProducer(Enrollment.Event.ShowDefaultErrorError)
		}

		eventProducer(Enrollment.Event.CloseDialog)

		return Enrollment.State.Failed
	}
}