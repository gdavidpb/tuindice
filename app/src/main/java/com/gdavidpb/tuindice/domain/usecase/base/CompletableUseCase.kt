package com.gdavidpb.tuindice.domain.usecase.base

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableCompletableObserver

abstract class CompletableUseCase<P>(
        private val subscribeOn: Scheduler,
        private val observeOn: Scheduler
) {
    private val disposables = CompositeDisposable()

    protected abstract fun buildUseCaseObservable(params: P): Completable

    fun execute(observer: DisposableCompletableObserver, params: P): Completable {
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