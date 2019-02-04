package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Continuous<T> {
    data class OnNext<T>(val value: T) : Continuous<T>()
    data class OnError<T>(val throwable: Throwable) : Continuous<T>()
    class OnCancel<T> : Continuous<T>()
    class OnComplete<T> : Continuous<T>()
}