package com.gdavidpb.tuindice.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gdavidpb.tuindice.domain.usecase.coroutines.*
import com.gdavidpb.tuindice.utils.MAX_MULTIPLE_EVENT

class LiveEvent<T, Q> : MultipleLiveEvent<Event<T, Q>>(MAX_MULTIPLE_EVENT)

typealias LiveResult<T, Q> = MutableLiveData<Result<T, Q>>
typealias LiveCompletable<Q> = MutableLiveData<Completable<Q>>
typealias LiveFlow<T, Q> = MutableLiveData<Flow<T, Q>>

/* LiveEvent */

@JvmName("postEmptyEvent")
fun <T, Q> LiveEvent<T, Q>.postEmpty() = postValue(Event.OnEmpty())

@JvmName("postSuccessEvent")
fun <T, Q> LiveEvent<T, Q>.postSuccess(value: T) = postValue(Event.OnSuccess(value))

@JvmName("postErrorEvent")
fun <T, Q> LiveEvent<T, Q>.postError(error: Q?) = postValue(Event.OnError(error))

@JvmName("postLoadingEvent")
fun <T, Q> LiveEvent<T, Q>.postLoading() = reset().also { postValue(Event.OnLoading()) }

@JvmName("postCancelEvent")
fun <T, Q> LiveEvent<T, Q>.postCancel() = postValue(Event.OnCancel())

/* LiveResult */

@JvmName("postEmptyResult")
fun <T, Q> LiveResult<T, Q>.postEmpty() = postValue(Result.OnEmpty())

@JvmName("postSuccessResult")
fun <T, Q> LiveResult<T, Q>.postSuccess(value: T) = postValue(Result.OnSuccess(value))

@JvmName("postErrorResult")
fun <T, Q> LiveResult<T, Q>.postError(error: Q?) = postValue(Result.OnError(error))

@JvmName("postLoadingResult")
fun <T, Q> LiveResult<T, Q>.postLoading() = postValue(Result.OnLoading())

@JvmName("postCancelResult")
fun <T, Q> LiveResult<T, Q>.postCancel() = postValue(Result.OnCancel())

/* LiveCompletable */

@JvmName("postCompleteCompletable")
fun <Q> LiveCompletable<Q>.postComplete() = postValue(Completable.OnComplete())

@JvmName("postErrorCompletable")
fun <Q> LiveCompletable<Q>.postError(error: Q?) = postValue(Completable.OnError(error))

@JvmName("postLoadingCompletable")
fun <Q> LiveCompletable<Q>.postLoading() = postValue(Completable.OnLoading())

@JvmName("postCancelCompletable")
fun <Q> LiveCompletable<Q>.postCancel() = postValue(Completable.OnCancel())

/* LiveFlow */

@JvmName("postStartFlow")
fun <T, Q> LiveFlow<T, Q>.postStart() = postValue(Flow.OnStart())

@JvmName("postNextFlow")
fun <T, Q> LiveFlow<T, Q>.postNext(value: T) = postValue(Flow.OnNext(value))

@JvmName("postCompleteFlow")
fun <T, Q> LiveFlow<T, Q>.postComplete() = postValue(Flow.OnComplete())

@JvmName("postErrorFlow")
fun <T, Q> LiveFlow<T, Q>.postError(error: Q?) = postValue(Flow.OnError(error))

@JvmName("postCancelFlow")
fun <T, Q> LiveFlow<T, Q>.postCancel() = postValue(Flow.OnCancel())

fun <T, L : LiveData<T>> FragmentActivity.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T, L : LiveData<T>> Fragment.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(viewLifecycleOwner, Observer(body))