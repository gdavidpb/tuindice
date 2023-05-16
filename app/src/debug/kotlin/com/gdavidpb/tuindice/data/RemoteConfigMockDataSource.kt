package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.utils.extension.getStringList
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigMockDataSource(
	private val remoteConfig: FirebaseRemoteConfig
) : ConfigRepository {
	override suspend fun tryFetch() {
	}

	override fun getTimeout(key: String): Long {
		return remoteConfig.getLong(key)
	}

	override fun getContactEmail(): String {
		return remoteConfig.getString(ConfigKeys.CONTACT_EMAIL)
	}

	override fun getContactSubject(): String {
		return remoteConfig.getString(ConfigKeys.CONTACT_SUBJECT)
	}

	override fun getLoadingMessages(): List<String> {
		return remoteConfig.getStringList(ConfigKeys.LOADING_MESSAGES)
	}

	override fun getTimeUpdateStalenessDays(): Int {
		return remoteConfig.getLong(ConfigKeys.TIME_UPDATE_STALENESS_DAYS).toInt()
	}

	override fun getSyncsToSuggestReview(): Int {
		return remoteConfig.getLong(ConfigKeys.SYNCS_TO_SUGGEST_REVIEW).toInt()
	}

	override fun getConnectionTimeout(): Long {
		return remoteConfig.getLong(ConfigKeys.TIME_OUT_CONNECTION)
	}
}