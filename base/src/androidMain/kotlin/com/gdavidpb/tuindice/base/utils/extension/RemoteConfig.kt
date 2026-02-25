package com.gdavidpb.tuindice.base.utils.extension

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.json.JSONArray
import java.io.IOException

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
