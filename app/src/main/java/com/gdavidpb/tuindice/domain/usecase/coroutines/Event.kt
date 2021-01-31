package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Event<T, Q> {
    data class OnSuccess<T, Q>(val value: T) : Event<T, Q>()
    data class OnError<T, Q>(val error: Q?) : Event<T, Q>()
    class OnLoading<T, Q> : Event<T, Q>()
    class OnCancel<T, Q> : Event<T, Q>()
}