package com.gdavidpb.tuindice.base.utils.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gdavidpb.tuindice.base.domain.usecase.base.*
import java.util.concurrent.atomic.AtomicBoolean

typealias LiveResult<T, Q> = MutableLiveData<Result<T, Q>>
typealias LiveCompletable<Q> = MutableLiveData<Completable<Q>>

open class LiveEvent<T, Q> : MutableLiveData<Event<T, Q>>() {
	private val lock = AtomicBoolean(false)

	override fun observe(owner: LifecycleOwner, observer: Observer<in Event<T, Q>>) {
		super.observe(owner) { t ->
			if (!lock.getAndSet(t is Event.OnSuccess))
				observer.onChanged(t)
		}
	}

	fun unlock() = lock.set(false)
}

/* LiveEvent */

@JvmName("postEmptyEvent")
fun <T, Q> LiveEvent<T, Q>.postEmpty() = postValue(Event.OnEmpty())

@JvmName("postSuccessEvent")
fun <T, Q> LiveEvent<T, Q>.postSuccess(value: T) = postValue(Event.OnSuccess(value))

@JvmName("postErrorEvent")
fun <T, Q> LiveEvent<T, Q>.postError(error: Q?) = postValue(Event.OnError(error))

@JvmName("postLoadingEvent")
fun <T, Q> LiveEvent<T, Q>.postLoading() = unlock().also { postValue(Event.OnLoading()) }

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

fun <T, L : MutableLiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) =
	liveData.observe(this, Observer(body))