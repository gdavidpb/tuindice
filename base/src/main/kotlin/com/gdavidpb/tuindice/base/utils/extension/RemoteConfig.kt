package com.gdavidpb.tuindice.base.utils.extension

import android.content.ComponentCallbacks
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.json.JSONArray
import org.koin.android.ext.android.get
import org.koin.core.context.GlobalContext.get
import java.io.IOException

inline fun <reified T : Any> config(crossinline block: ConfigRepository.() -> T) =
	lazy {
		val configRepository = get().get<ConfigRepository>(ConfigRepository::class)

		block(configRepository)
	}

inline fun <reified T : Any> ComponentCallbacks.config(crossinline block: ConfigRepository.() -> T) =
	lazy { block(get()) }

fun FirebaseRemoteConfig.getStringList(key: String): List<String> {
	val json = getString(key)

	return runCatching {
		val jsonArray = JSONArray(json)

		(0 until jsonArray.length())
			.map { i -> jsonArray.getString(i) }
	}.getOrElse { throwable ->
		throw IOException("Could not read '$key'. ${throwable.message}", throwable)
	}
}