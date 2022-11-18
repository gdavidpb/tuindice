package com.gdavidpb.tuindice.base.domain.usecase.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicInteger

open class MultipleLiveEvent<T>(private val times: Int) : MutableLiveData<T>() {

	private val counter = AtomicInteger(0)

	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
		super.observe(owner) { t ->
			if (counter.getAndIncrement() < times)
				observer.onChanged(t)
		}
	}

	fun reset() {
		counter.set(0)
	}
}