package com.gdavidpb.tuindice.base.domain.usecase.base

sealed class Completable<Q> {
    class OnComplete<Q> : Completable<Q>()
    data class OnError<Q>(val error: Q?) : Completable<Q>()
    class OnLoading<Q> : Completable<Q>()
}