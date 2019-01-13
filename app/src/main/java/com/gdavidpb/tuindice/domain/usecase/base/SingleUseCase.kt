package com.gdavidpb.tuindice.domain.usecase.base

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver

abstract class SingleUseCase<Q, P>(
        private val subscribeOn: Scheduler,
        private val observeOn: Scheduler
) {
    private val disposables = CompositeDisposable()

    protected abstract fun buildUseCaseObservable(params: P): Single<Q>

    fun execute(observer: DisposableSingleObserver<Q>, params: P): Single<Q> {
        return buildUseCaseObservable(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .also {
                    it.subscribeWith(observer).let(::addDisposable)
                }
    }

    fun <T> Single<T>.ensureSchedulers(): Single<T> {
        return apply {
            observeOn(observeOn)
            subscribeOn(subscribeOn)
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