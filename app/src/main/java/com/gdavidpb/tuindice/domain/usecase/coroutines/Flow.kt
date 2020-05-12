package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Flow<T> {
    class OnStart<T> : Flow<T>()
    data class OnNext<T>(val value: T) : Flow<T>()
    data class OnComplete<T>(val value: T?) : Flow<T>()
    data class OnError<T>(val throwable: Throwable) : Flow<T>()
    class OnCancel<T> : Flow<T>()
}