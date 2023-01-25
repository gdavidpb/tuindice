package com.gdavidpb.tuindice.data.fcm

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.utils.SettingsKeys
import com.gdavidpb.tuindice.data.fcm.source.LocalDataSource

class FCMLocalDataSource(
	private val sharedPreferences: SharedPreferences
) : LocalDataSource {
	override suspend fun getSubscribedTopics(): List<String> {
		val topics = sharedPreferences
			.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		return topics.toList()
	}

	override suspend fun saveSubscriptionTopic(topic: String) {
		val topics = sharedPreferences
			.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		sharedPreferences.edit {
			putStringSet(SettingsKeys.SUBSCRIBED_TOPICS, topics + topic)
		}
	}

	override suspend fun isSubscribedToTopic(topic: String): Boolean {
		val topics = sharedPreferences
			.getStringSet(SettingsKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		return topics.contains(topic)
	}
}