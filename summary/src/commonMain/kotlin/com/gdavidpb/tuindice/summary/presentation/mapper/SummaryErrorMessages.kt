package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_no_service
import tuindice.summary.generated.resources.snack_profile_picture_not_image
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_size_exceeded

internal suspend fun UpdateUserUseCaseError?.toRefreshMessage(): String {
	return when (this) {
		is UpdateUserUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				commonServiceUnavailableMessage()
			} else {
				commonNetworkUnavailableMessage()
			}

		UpdateUserUseCaseError.Timeout ->
			commonTimeoutMessage()

		UpdateUserUseCaseError.Unavailable ->
			commonServiceUnavailableMessage()

		UpdateUserUseCaseError.NotFound ->
			getString(Res.string.snack_no_service)

		null ->
			commonUnexpectedErrorMessage()
	}
}

internal suspend fun ProfilePictureUseCaseError?.toUploadMessage(): String {
	return when (this) {
		is ProfilePictureUseCaseError.Timeout ->
			commonTimeoutMessage()

		is ProfilePictureUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				commonServiceUnavailableMessage()
			} else {
				commonNetworkUnavailableMessage()
			}

		ProfilePictureUseCaseError.InvalidImage ->
			getString(Res.string.snack_profile_picture_not_image)

		ProfilePictureUseCaseError.SizeExceeded ->
			getString(Res.string.snack_profile_picture_size_exceeded)

		else ->
			commonUnexpectedErrorMessage()
	}
}

internal suspend fun ProfilePictureUseCaseError?.toRemoveMessage(): String {
	return when (this) {
		ProfilePictureUseCaseError.NotFound ->
			getString(Res.string.snack_profile_picture_removed)

		is ProfilePictureUseCaseError.Timeout ->
			commonTimeoutMessage()

		is ProfilePictureUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				commonServiceUnavailableMessage()
			} else {
				commonNetworkUnavailableMessage()
			}

		else ->
			commonUnexpectedErrorMessage()
	}
}
