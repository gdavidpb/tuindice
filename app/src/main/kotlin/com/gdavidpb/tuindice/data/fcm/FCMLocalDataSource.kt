package com.gdavidpb.tuindice.data.fcm

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.fcm.source.LocalDataSource

class FCMLocalDataSource(
	private val sharedPreferences: SharedPreferences
) : LocalDataSource {
	override suspend fun getSubscribedTopics(): List<String> {
		val topics = sharedPreferences
			.getStringSet(PreferencesKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		return topics.toList()
	}

	override suspend fun saveSubscriptionTopic(topic: String) {
		val topics = sharedPreferences
			.getStringSet(PreferencesKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		sharedPreferences.edit {
			putStringSet(PreferencesKeys.SUBSCRIBED_TOPICS, topics + topic)
		}
	}

	override suspend fun isSubscribedToTopic(topic: String): Boolean {
		val topics = sharedPreferences
			.getStringSet(PreferencesKeys.SUBSCRIBED_TOPICS, setOf()) ?: setOf()

		return topics.contains(topic)
	}
}