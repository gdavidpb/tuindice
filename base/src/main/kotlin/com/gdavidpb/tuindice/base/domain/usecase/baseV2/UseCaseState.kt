package com.gdavidpb.tuindice.base.domain.usecase.baseV2

sealed class UseCaseState<T, E> {
	data class Data<T, E>(val value: T) : UseCaseState<T, E>()
	data class Error<T, E>(val error: E?) : UseCaseState<T, E>()
	class Loading<T, E> : UseCaseState<T, E>()
}