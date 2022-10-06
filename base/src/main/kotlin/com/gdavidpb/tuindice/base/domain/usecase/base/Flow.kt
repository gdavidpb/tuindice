package com.gdavidpb.tuindice.base.domain.usecase.base

sealed class Flow<T, Q> {
    class OnStart<T, Q> : Flow<T, Q>()
    data class OnNext<T, Q>(val value: T) : Flow<T, Q>()
    class OnComplete<T, Q> : Flow<T, Q>()
    data class OnError<T, Q>(val error: Q?) : Flow<T, Q>()
}