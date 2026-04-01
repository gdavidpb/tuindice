package com.gdavidpb.tuindice.base.data.source.config

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository

import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.serialization.json.Json

class DebugRemoteConfigDataSource(
	private val defaults: DefaultRemoteConfigValues,
	private val sourceName: String
) : RemoteConfigDataRepository {
	private val logger = appLogger(tag = "RemoteConfig")

	override suspend fun fetch() {
		logger.i { "[$sourceName] fetch(): using debug defaults instead of Firebase Remote Config." }
	}

	override fun getString(key: String): String? {
		val value = when (key) {
			RemoteConfigKeys.TIME_OUT_CONNECTION -> defaults.timeoutMillis.toString()
			RemoteConfigKeys.CONTACT_EMAIL -> defaults.contactEmail
			RemoteConfigKeys.CONTACT_SUBJECT -> defaults.contactSubject
			RemoteConfigKeys.LOADING_MESSAGES -> Json.encodeToString(defaults.loadingMessages)
			RemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS -> defaults.updateStalenessDays.toString()
			RemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW -> defaults.syncsToSuggestReview.toString()
			else -> null
		}

		logger.d { "[$sourceName] getString(key=$key) -> ${value ?: "<null>"}" }
		return value
	}
}
