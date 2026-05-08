package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.pensum.generated.resources.Res
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
