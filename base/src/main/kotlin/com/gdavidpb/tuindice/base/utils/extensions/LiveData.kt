package com.gdavidpb.tuindice.base.utils.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gdavidpb.tuindice.base.domain.usecase.base.*
import com.gdavidpb.tuindice.base.utils.MAX_MULTIPLE_EVENT

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

/* LiveResult */

@JvmName("postEmptyResult")
fun <T, Q> LiveResult<T, Q>.postEmpty() = postValue(Result.OnEmpty())

@JvmName("postSuccessResult")
fun <T, Q> LiveResult<T, Q>.postSuccess(value: T) = postValue(Result.OnSuccess(value))

@JvmName("postErrorResult")
fun <T, Q> LiveResult<T, Q>.postError(error: Q?) = postValue(Result.OnError(error))

@JvmName("postLoadingResult")
fun <T, Q> LiveResult<T, Q>.postLoading() = postValue(Result.OnLoading())

/* LiveCompletable */

@JvmName("postCompleteCompletable")
fun <Q> LiveCompletable<Q>.postComplete() = postValue(Completable.OnComplete())

@JvmName("postErrorCompletable")
fun <Q> LiveCompletable<Q>.postError(error: Q?) = postValue(Completable.OnError(error))

@JvmName("postLoadingCompletable")
fun <Q> LiveCompletable<Q>.postLoading() = postValue(Completable.OnLoading())

/* LiveFlow */

@JvmName("postStartFlow")
fun <T, Q> LiveFlow<T, Q>.postStart() = postValue(Flow.OnStart())

@JvmName("postNextFlow")
fun <T, Q> LiveFlow<T, Q>.postNext(value: T) = postValue(Flow.OnNext(value))

@JvmName("postCompleteFlow")
fun <T, Q> LiveFlow<T, Q>.postComplete() = postValue(Flow.OnComplete())

@JvmName("postErrorFlow")
fun <T, Q> LiveFlow<T, Q>.postError(error: Q?) = postValue(Flow.OnError(error))

fun <T, L : MutableLiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))