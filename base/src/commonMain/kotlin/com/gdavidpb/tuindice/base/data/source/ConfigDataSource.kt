package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.contract.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class ConfigDataSource(
	private val remoteConfigDataSource: RemoteConfigDataSource,
	private val defaults: DefaultRemoteConfigValues
) : ConfigRepository {
	override suspend fun tryFetch() {
		runCatching {
			remoteConfigDataSource.fetch()
		}
	}

	override fun getTimeout(): Long {
		return remoteConfigDataSource.getString(RemoteConfigKeys.TIME_OUT_CONNECTION)
			?.toLongOrNull()
			?: defaults.timeoutMillis
	}

	override fun getContactEmail(): String {
		return remoteConfigDataSource.getString(RemoteConfigKeys.CONTACT_EMAIL)
			?: defaults.contactEmail
	}

	override fun getContactSubject(): String {
		return remoteConfigDataSource.getString(RemoteConfigKeys.CONTACT_SUBJECT)
			?: defaults.contactSubject
	}

	override fun getLoadingMessages(): List<String> {
		return remoteConfigDataSource.getString(RemoteConfigKeys.LOADING_MESSAGES)
			?.toStringListOrNull()
			?.takeIf { it.isNotEmpty() }
			?: defaults.loadingMessages
	}

	override fun getTimeUpdateStalenessDays(): Int {
		return remoteConfigDataSource.getString(RemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS)
			?.toLongOrNull()
			?.toInt()
			?: defaults.updateStalenessDays
	}

	override fun getSyncsToSuggestReview(): Int {
		return remoteConfigDataSource.getString(RemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW)
			?.toLongOrNull()
			?.toInt()
			?: defaults.syncsToSuggestReview
	}
}

private fun String.toStringListOrNull(): List<String>? {
	return runCatching {
		Json.parseToJsonElement(this)
			.jsonArray
			.map { jsonElement ->
				jsonElement.jsonPrimitive.content
			}
	}.getOrNull()
}
