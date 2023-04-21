package com.gdavidpb.tuindice.enrollmentproof.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

class EnrollmentProcessor(
	override val eventChannel: (Enrollment.Event) -> Unit
) : BaseProcessor<String, FetchEnrollmentProofError, Enrollment.State, Enrollment.Event>(eventChannel) {
	override suspend fun processLoadingState(state: UseCaseState.Loading<String, FetchEnrollmentProofError>): Enrollment.State {
		return Enrollment.State.Fetching
	}

	override suspend fun processDataState(state: UseCaseState.Data<String, FetchEnrollmentProofError>): Enrollment.State {
		eventChannel(Enrollment.Event.OpenEnrollmentProof(path = state.value))
		eventChannel(Enrollment.Event.CloseDialog)

		return Enrollment.State.Fetched
	}

	override suspend fun processErrorState(state: UseCaseState.Error<String, FetchEnrollmentProofError>): Enrollment.State {
		when (val error = state.error) {
			is FetchEnrollmentProofError.AccountDisabled ->
				eventChannel(Enrollment.Event.NavigateToAccountDisabled)

			is FetchEnrollmentProofError.NoConnection ->
				eventChannel(Enrollment.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

			is FetchEnrollmentProofError.NotFound ->
				eventChannel(Enrollment.Event.ShowNotFoundSnackBar)

			is FetchEnrollmentProofError.UnsupportedFile ->
				eventChannel(Enrollment.Event.ShowUnsupportedFileSnackBar)

			is FetchEnrollmentProofError.OutdatedPassword ->
				eventChannel(Enrollment.Event.NavigateToOutdatedPassword)

			is FetchEnrollmentProofError.Timeout ->
				eventChannel(Enrollment.Event.ShowTimeoutSnackBar)

			is FetchEnrollmentProofError.Unavailable ->
				eventChannel(Enrollment.Event.ShowUnavailableSnackBar)

			else ->
				eventChannel(Enrollment.Event.ShowDefaultErrorError)
		}

		eventChannel(Enrollment.Event.CloseDialog)

		return Enrollment.State.Failed
	}
}