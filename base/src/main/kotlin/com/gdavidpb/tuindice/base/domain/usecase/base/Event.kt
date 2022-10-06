package com.gdavidpb.tuindice.base.domain.usecase.base

sealed class Event<T, Q> {
    class OnEmpty<T, Q> : Event<T, Q>()
    data class OnSuccess<T, Q>(val value: T) : Event<T, Q>()
    data class OnError<T, Q>(val error: Q?) : Event<T, Q>()
    class OnLoading<T, Q> : Event<T, Q>()
}