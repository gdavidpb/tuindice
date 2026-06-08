package com.gdavidpb.tuindice.pensum.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum

internal fun Pensum.State.loadingOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Loading
}

internal fun Pensum.State.failedOrContent(message: UiText): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Failed(message = message)
}

internal fun Pensum.State.snackBarEffectOrNull(
	error: UpdatePensumUseCaseError?
): Pensum.Effect.ShowSnackBar? {
	return if (this is Pensum.State.Content && error.shouldNotifyVisibleContent()) {
		Pensum.Effect.ShowSnackBar(error.toSnackBarMessage())
	} else {
		null
	}
}

private fun UpdatePensumUseCaseError?.shouldNotifyVisibleContent(): Boolean {
	return when (this) {
		is UpdatePensumUseCaseError.NoConnection -> isNetworkAvailable
		UpdatePensumUseCaseError.NotFound,
		UpdatePensumUseCaseError.Timeout,
		UpdatePensumUseCaseError.Unavailable,
		null,
		-> true
	}
}
