package com.gdavidpb.tuindice.domain.usecase.coroutines

sealed class Completable<Q> {
    class OnComplete<Q> : Completable<Q>()
    data class OnError<Q>(val error: Q?) : Completable<Q>()
    class OnLoading<Q> : Completable<Q>()
    class OnTimeout<Q> : Completable<Q>()
    class OnCancel<Q> : Completable<Q>()
}