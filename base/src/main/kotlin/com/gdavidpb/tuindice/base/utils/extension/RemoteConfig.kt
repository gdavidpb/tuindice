package com.gdavidpb.tuindice.base.utils.extension

import android.content.ComponentCallbacks
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import org.koin.android.ext.android.get
import java.io.IOException

inline fun <reified T : Any> ComponentCallbacks.config(crossinline block: ConfigRepository.() -> T) =
	lazy { block(get()) }

fun FirebaseRemoteConfig.getStringList(key: String, googleJson: Gson): List<String> {
	val json = getString(key)

	return runCatching {
		googleJson.fromJson(json, Array<String>::class.java).toList()
	}.getOrElse { throwable ->
		throw IOException("Could not read '$key'. ${throwable.message}", throwable)
	}
}