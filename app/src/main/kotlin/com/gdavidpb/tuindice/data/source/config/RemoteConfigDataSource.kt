package com.gdavidpb.tuindice.data.source.config

import com.gdavidpb.tuindice.ConfigKeys
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.utils.extension.awaitOrNull
import com.gdavidpb.tuindice.base.utils.extension.getStringList
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson

class RemoteConfigDataSource(
	private val googleJson: Gson,
	private val remoteConfig: FirebaseRemoteConfig
) : ConfigRepository {
	override suspend fun tryFetch() {
		remoteConfig.fetchAndActivate().awaitOrNull()
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

	override fun getIssuesList(): List<String> {
		return remoteConfig.getStringList(ConfigKeys.ISSUES_LIST, googleJson)
	}

	override fun getLoadingMessages(): List<String> {
		return remoteConfig.getStringList(ConfigKeys.LOADING_MESSAGES, googleJson)
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