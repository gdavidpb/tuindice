package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.pensum_failed_network_unavailable
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable
import tuindice.pensum.generated.resources.pensum_failed_timeout
import tuindice.pensum.generated.resources.snack_default_error
import tuindice.pensum.generated.resources.snack_network_unavailable
import tuindice.pensum.generated.resources.snack_service_unavailable
import tuindice.pensum.generated.resources.snack_timeout

internal suspend fun UpdatePensumUseCaseError?.toSnackBarMessage(): String {
	return when (this) {
		UpdatePensumUseCaseError.NotFound ->
			getString(Res.string.snack_default_error)

		is UpdatePensumUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		is UpdatePensumUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		is UpdatePensumUseCaseError.Unavailable ->
			getString(Res.string.snack_service_unavailable)

		null ->
			getString(Res.string.snack_default_error)
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

		is UpdatePensumUseCaseError.Timeout ->
			UiText.Resource(Res.string.pensum_failed_timeout)

		is UpdatePensumUseCaseError.Unavailable ->
			UiText.Resource(Res.string.pensum_failed_service_unavailable)

		null ->
			UiText.Resource(Res.string.pensum_failed_message)
	}
}
