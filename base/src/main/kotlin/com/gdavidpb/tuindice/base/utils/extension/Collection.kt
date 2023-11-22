package com.gdavidpb.tuindice.base.utils.extension

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
