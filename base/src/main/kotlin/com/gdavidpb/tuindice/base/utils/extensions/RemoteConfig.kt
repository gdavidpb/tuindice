package com.gdavidpb.tuindice.base.utils.extensions

import android.content.ComponentCallbacks
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import okio.IOException
import org.koin.android.ext.android.get

inline fun <reified T : Any> ComponentCallbacks.config(key: String) =
	lazy { getConfig<T>(get(), key) }

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T : Any> getConfig(configRepository: ConfigRepository, key: String): T {
	return runCatching {
		when (T::class) {
			String::class -> configRepository.getString(key)
			List::class -> configRepository.getStringList(key)
			Int::class -> configRepository.getLong(key).toInt()
			Long::class -> configRepository.getLong(key)
			Double::class -> configRepository.getDouble(key)
			Float::class -> configRepository.getDouble(key).toFloat()
			Boolean::class -> configRepository.getBoolean(key)
			else -> throw IllegalArgumentException("Unsupported value type '${T::class.simpleName}'.")
		} as T
	}.getOrElse { throwable ->
		throw IOException("Could not load config '$key'. ${throwable.message}", throwable)
	}
}