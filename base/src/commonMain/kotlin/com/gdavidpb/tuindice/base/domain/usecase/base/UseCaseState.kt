package com.gdavidpb.tuindice.base.domain.usecase.base

sealed class UseCaseState<out T, out E : UseCaseError> {
	data object Loading : UseCaseState<Nothing, Nothing>()
	data class Data<out T>(val value: T) : UseCaseState<T, Nothing>()
	data class Error<out E : UseCaseError>(val error: E?) : UseCaseState<Nothing, E>()
}
