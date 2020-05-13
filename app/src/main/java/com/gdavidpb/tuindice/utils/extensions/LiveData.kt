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
typealias LiveFlow<T> = MutableLiveData<Flow<T>>

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

/* LiveFlow */

@JvmName("postStartFlow")
fun <T> LiveFlow<T>.postStart() = postValue(Flow.OnStart())

@JvmName("postNextFlow")
fun <T> LiveFlow<T>.postNext(value: T) = postValue(Flow.OnNext(value))

@JvmName("postCompleteFlow")
fun <T> LiveFlow<T>.postComplete() = postValue(Flow.OnComplete())

@JvmName("postThrowableFlow")
fun <T> LiveFlow<T>.postThrowable(throwable: Throwable) = postValue(Flow.OnError(throwable))

@JvmName("postCancelFlow")
fun <T> LiveFlow<T>.postCancel() = postValue(Flow.OnCancel())

fun <T, L : LiveData<T>> FragmentActivity.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T, L : LiveData<T>> Fragment.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(viewLifecycleOwner, Observer(body))