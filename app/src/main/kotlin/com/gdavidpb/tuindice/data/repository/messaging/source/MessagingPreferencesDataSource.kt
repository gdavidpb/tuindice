package com.gdavidpb.tuindice.data.repository.messaging.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource

class MessagingPreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : LocalDataSource {
	override suspend fun isEnrolled(): Boolean {
		return sharedPreferences
			.getBoolean(PreferencesKeys.IS_ENROLLED, false)
	}

	override suspend fun markAsEnrolled() {
		sharedPreferences.edit {
			putBoolean(PreferencesKeys.IS_ENROLLED, true)
		}
	}

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