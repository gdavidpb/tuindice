package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.pensum_local_data_warning_network
import tuindice.pensum.generated.resources.pensum_local_data_warning_service
import tuindice.pensum.generated.resources.pensum_local_data_warning_timeout

fun UseCaseState<*, UpdatePensumUseCaseError>.toPensumRefreshMutation(): Mutation<Pensum.State> =
	when (this) {
		is UseCaseState.Loading -> { state -> state.loadingOrContent() }
		is UseCaseState.Data -> { state -> state.idleOrContent() }
		is UseCaseState.Error -> { state ->
			if (error == UpdatePensumUseCaseError.NotFound) {
				Pensum.State.Empty
			} else {
				state.failedOrContent(error)
			}
		}
	}

private fun Pensum.State.loadingOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> copy(isRefreshing = true, localDataMessage = null)
	Pensum.State.Empty,
	Pensum.State.RecordDataUnavailable,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Loading
}

private fun Pensum.State.idleOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> copy(isRefreshing = false, localDataMessage = null)
	Pensum.State.Empty,
	Pensum.State.RecordDataUnavailable,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> this
}

private fun Pensum.State.failedOrContent(error: UpdatePensumUseCaseError?): Pensum.State = when (this) {
	is Pensum.State.Content -> copy(
		isRefreshing = false,
		localDataMessage = error.toLocalDataWarningMessage()
	)
	Pensum.State.Empty,
	Pensum.State.RecordDataUnavailable,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Failed(message = error.toFailedMessage())
}

internal fun UpdatePensumUseCaseError?.toLocalDataWarningMessage(): UiText {
	return when (this) {
		UpdatePensumUseCaseError.NotFound ->
			UiText.Resource(Res.string.pensum_local_data_warning_service)

		is UpdatePensumUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				UiText.Resource(Res.string.pensum_local_data_warning_service)
			else
				UiText.Resource(Res.string.pensum_local_data_warning_network)

		UpdatePensumUseCaseError.Timeout ->
			UiText.Resource(Res.string.pensum_local_data_warning_timeout)

		UpdatePensumUseCaseError.Unavailable ->
			UiText.Resource(Res.string.pensum_local_data_warning_service)

		null ->
			UiText.Resource(Res.string.pensum_local_data_warning_service)
	}
}

internal fun UpdatePensumUseCaseError?.toFailedMessage(): UiText {
	return when (this) {
		UpdatePensumUseCaseError.NotFound ->
			UiText.Resource(Res.string.pensum_failed_message)

		is UpdatePensumUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				UiText.Resource(Res.string.pensum_failed_service_unavailable)
			else
				UiText.Resource(Res.string.pensum_failed_network_unavailable)

		UpdatePensumUseCaseError.Timeout ->
			UiText.Resource(Res.string.pensum_failed_timeout)

		UpdatePensumUseCaseError.Unavailable ->
			UiText.Resource(Res.string.pensum_failed_service_unavailable)

		null ->
			UiText.Resource(Res.string.pensum_failed_message)
	}
}
