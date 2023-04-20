package com.gdavidpb.tuindice.enrollmentproof.presentation.processor

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.processor.BaseProcessor
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.GetEnrollmentError
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

class EnrollmentProcessor(
	override val eventChannel: (Enrollment.Event) -> Unit
) : BaseProcessor<String, GetEnrollmentError, Enrollment.State, Enrollment.Event>(eventChannel) {
	override suspend fun processLoadingState(state: UseCaseState.Loading<String, GetEnrollmentError>): Enrollment.State {
		return Enrollment.State.Fetching
	}

	override suspend fun processDataState(state: UseCaseState.Data<String, GetEnrollmentError>): Enrollment.State {
		eventChannel(Enrollment.Event.OpenEnrollmentProof(path = state.value))
		eventChannel(Enrollment.Event.CloseDialog)

		return Enrollment.State.Fetched
	}

	override suspend fun processErrorState(state: UseCaseState.Error<String, GetEnrollmentError>): Enrollment.State {
		when (val error = state.error) {
			is GetEnrollmentError.AccountDisabled ->
				eventChannel(Enrollment.Event.NavigateToAccountDisabled)

			is GetEnrollmentError.NoConnection ->
				eventChannel(Enrollment.Event.ShowNoConnectionSnackBar(isNetworkAvailable = error.isNetworkAvailable))

			is GetEnrollmentError.NotFound ->
				eventChannel(Enrollment.Event.ShowNotFoundSnackBar)

			is GetEnrollmentError.UnsupportedFile ->
				eventChannel(Enrollment.Event.ShowUnsupportedFileSnackBar)

			is GetEnrollmentError.OutdatedPassword ->
				eventChannel(Enrollment.Event.NavigateToOutdatedPassword)

			is GetEnrollmentError.Timeout ->
				eventChannel(Enrollment.Event.ShowTimeoutSnackBar)

			is GetEnrollmentError.Unavailable ->
				eventChannel(Enrollment.Event.ShowUnavailableSnackBar)

			else ->
				eventChannel(Enrollment.Event.ShowDefaultErrorError)
		}

		eventChannel(Enrollment.Event.CloseDialog)

		return Enrollment.State.Failed
	}
}