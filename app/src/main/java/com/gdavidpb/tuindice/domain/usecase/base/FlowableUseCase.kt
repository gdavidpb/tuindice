package com.gdavidpb.tuindice.domain.usecase.base

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver

abstract class FlowableUseCase<Q, P>(
        private val subscribeOn: Scheduler,
        private val observeOn: Scheduler
) {
    private val disposables = CompositeDisposable()

    protected abstract fun buildUseCaseObservable(params: P): Flowable<Q>

    fun execute(observer: DisposableObserver<Q>, params: P): Flowable<Q> {
        return buildUseCaseObservable(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .also {
                    it.subscribe({ next ->
                        /* OnNext */
                        observer.onNext(next)
                    }, { error ->
                        /* OnError */
                        observer.onError(error)
                    }, {
                        /* OnComplete */
                        observer.onComplete()
                    }).let(::addDisposable)
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