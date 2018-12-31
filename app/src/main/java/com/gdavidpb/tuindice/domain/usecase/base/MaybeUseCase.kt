package com.gdavidpb.tuindice.domain.usecase.base

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver

abstract class MaybeUseCase<Q, P>(
        private val subscribeOn: Scheduler,
        private val observeOn: Scheduler
) {
    private val disposables = CompositeDisposable()

    protected abstract fun buildUseCaseObservable(params: P): Maybe<Q>

    fun execute(observer: DisposableMaybeObserver<Q>, params: P): Maybe<Q> {
        return buildUseCaseObservable(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .also {
                    it.subscribeWith(observer).let(::addDisposable)
                }
    }

    fun dispose() {
        if (!disposables.isDisposed)
            disposables.dispose()
    }

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }
}