package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Continuous<T> {
    class OnStart<T> : Continuous<T>()
    data class OnNext<T>(val value: T) : Continuous<T>()
    data class OnComplete<T>(val value: T?) : Continuous<T>()
    data class OnError<T>(val throwable: Throwable) : Continuous<T>()
    class OnCancel<T> : Continuous<T>()
}