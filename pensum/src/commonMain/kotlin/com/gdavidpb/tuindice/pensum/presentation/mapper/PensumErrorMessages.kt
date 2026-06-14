package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.pensum_local_data_warning_network
import tuindice.pensum.generated.resources.pensum_local_data_warning_service
import tuindice.pensum.generated.resources.pensum_local_data_warning_timeout

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
