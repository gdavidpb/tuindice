package com.gdavidpb.tuindice.utils.extensions

import android.app.Activity
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.domain.repository.ConfigRepository
import okio.IOException
import org.koin.android.ext.android.get

inline fun <reified T : Any> Activity.config(key: String) = lazy { getConfig<T>(get(), key) }
inline fun <reified T : Any> Fragment.config(key: String) = lazy { getConfig<T>(get(), key) }

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