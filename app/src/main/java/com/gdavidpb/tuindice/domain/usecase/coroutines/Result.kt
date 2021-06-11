package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Result<T, Q> {
    class OnEmpty<T, Q> : Result<T, Q>()
    data class OnSuccess<T, Q>(val value: T) : Result<T, Q>()
    data class OnError<T, Q>(val error: Q?) : Result<T, Q>()
    class OnLoading<T, Q> : Result<T, Q>()
}