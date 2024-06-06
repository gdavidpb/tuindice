package com.gdavidpb.tuindice.base.domain.usecase.base

sealed class UseCaseState<T, E : UseCaseError> {
	class Loading<T, E : UseCaseError> : UseCaseState<T, E>()
	data class Data<T, E : UseCaseError>(val value: T) : UseCaseState<T, E>()
	data class Error<T, E : UseCaseError>(val error: E?) : UseCaseState<T, E>()
}