package com.gdavidpb.tuindice.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gdavidpb.tuindice.domain.usecase.coroutines.*
import com.gdavidpb.tuindice.utils.MAX_MULTIPLE_EVENT

class LiveEvent<T> : MultipleLiveEvent<Event<T>>(MAX_MULTIPLE_EVENT)

typealias LiveResult<T> = MutableLiveData<Result<T>>
typealias LiveCompletable = MutableLiveData<Completable>
typealias LiveContinuous<T> = MutableLiveData<Continuous<T>>

/* LiveEvent */

@JvmName("postSuccessEvent")
fun <T> LiveEvent<T>.postSuccess(value: T) = postValue(Event.OnSuccess(value))

@JvmName("postThrowableEvent")
fun <T> LiveEvent<T>.postThrowable(throwable: Throwable) = postValue(Event.OnError(throwable))

@JvmName("postLoadingEvent")
fun <T> LiveEvent<T>.postLoading() = reset().also { postValue(Event.OnLoading()) }

@JvmName("postCancelEvent")
fun <T> LiveEvent<T>.postCancel() = postValue(Event.OnCancel())

/* LiveResult */

@JvmName("postSuccessResult")
fun <T> LiveResult<T>.postSuccess(value: T) = postValue(Result.OnSuccess(value))

@JvmName("postThrowableResult")
fun <T> LiveResult<T>.postThrowable(throwable: Throwable) = postValue(Result.OnError(throwable))

@JvmName("postLoadingResult")
fun <T> LiveResult<T>.postLoading() = postValue(Result.OnLoading())

@JvmName("postCancelResult")
fun <T> LiveResult<T>.postCancel() = postValue(Result.OnCancel())

/* LiveCompletable */

@JvmName("postCompleteCompletable")
fun LiveCompletable.postComplete() = postValue(Completable.OnComplete)

@JvmName("postThrowableCompletable")
fun LiveCompletable.postThrowable(throwable: Throwable) = postValue(Completable.OnError(throwable))

@JvmName("postLoadingCompletable")
fun LiveCompletable.postLoading() = postValue(Completable.OnLoading)

@JvmName("postCancelCompletable")
fun LiveCompletable.postCancel() = postValue(Completable.OnCancel)

/* LiveContinuous */

@JvmName("postStartContinuous")
fun <T> LiveContinuous<T>.postStart() = postValue(Continuous.OnStart())

@JvmName("postNextContinuous")
fun <T> LiveContinuous<T>.postNext(value: T) = postValue(Continuous.OnNext(value))

@JvmName("postCompleteContinuous")
fun <T> LiveContinuous<T>.postComplete() {
    val lastValue = value as? Continuous.OnNext<T>
    val completeValue = Continuous.OnComplete(lastValue?.value)

    postValue(completeValue)
}

@JvmName("postThrowableContinuous")
fun <T> LiveContinuous<T>.postThrowable(throwable: Throwable) = postValue(Continuous.OnError(throwable))

@JvmName("postCancelContinuous")
fun <T> LiveContinuous<T>.postCancel() = postValue(Continuous.OnCancel())

fun <T, L : LiveData<T>> FragmentActivity.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T, L : LiveData<T>> Fragment.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(viewLifecycleOwner, Observer(body))