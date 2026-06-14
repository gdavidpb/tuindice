package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_network_unavailable
import tuindice.summary.generated.resources.snack_no_service
import tuindice.summary.generated.resources.snack_profile_picture_not_image
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_size_exceeded
import tuindice.summary.generated.resources.snack_service_unavailable
import tuindice.summary.generated.resources.snack_timeout

internal suspend fun UpdateUserUseCaseError?.toRefreshMessage(): String {
	return when (this) {
		is UpdateUserUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		UpdateUserUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		UpdateUserUseCaseError.Unavailable ->
			getString(Res.string.snack_service_unavailable)

		UpdateUserUseCaseError.NotFound ->
			getString(Res.string.snack_no_service)

		null ->
			getString(Res.string.snack_default_error)
	}
}

internal suspend fun ProfilePictureUseCaseError?.toUploadMessage(): String {
	return when (this) {
		is ProfilePictureUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		is ProfilePictureUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		ProfilePictureUseCaseError.InvalidImage ->
			getString(Res.string.snack_profile_picture_not_image)

		ProfilePictureUseCaseError.SizeExceeded ->
			getString(Res.string.snack_profile_picture_size_exceeded)

		else ->
			getString(Res.string.snack_default_error)
	}
}

internal suspend fun ProfilePictureUseCaseError?.toRemoveMessage(): String {
	return when (this) {
		ProfilePictureUseCaseError.NotFound ->
			getString(Res.string.snack_profile_picture_removed)

		is ProfilePictureUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		is ProfilePictureUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		else ->
			getString(Res.string.snack_default_error)
	}
}
