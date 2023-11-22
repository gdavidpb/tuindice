package com.gdavidpb.tuindice.base.utils.extension

import android.util.LruCache

inline fun <T> MutableList<T>.selfMapNotNull(operator: (T) -> T?): List<T> {
	val iterator = listIterator()

	while (iterator.hasNext()) {
		val oldValue = iterator.next()
		val newValue = operator(oldValue)

		when {
			newValue == null -> iterator.remove()
			newValue !== oldValue -> iterator.set(newValue)
		}
	}

	return this
}

inline fun <K, V> LruCache<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
	return get(key) ?: defaultValue().also { value -> put(key, value) }
}