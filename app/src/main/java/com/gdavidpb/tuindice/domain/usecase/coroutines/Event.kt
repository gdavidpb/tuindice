package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Event<T> {
    data class OnSuccess<T>(val value: T) : Event<T>()
    data class OnError<T>(val throwable: Throwable) : Event<T>()
    class OnLoading<T> : Event<T>()
    class OnCancel<T> : Event<T>()
}