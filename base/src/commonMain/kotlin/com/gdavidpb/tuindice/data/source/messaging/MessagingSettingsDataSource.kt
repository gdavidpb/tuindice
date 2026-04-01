package com.gdavidpb.tuindice.data.source.messaging

import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.repository.messaging.MessagingLocalDataRepository
import com.russhwolf.settings.Settings

class MessagingSettingsDataSource(
	private val settings: Settings
) : MessagingLocalDataRepository {
	override suspend fun isSubscribed(): Boolean {
		return settings.getBooleanOrNull(PreferencesKeys.IS_SUBSCRIBED) ?: false
	}

	override suspend fun getSubscribedToken(): String? {
		return settings.getStringOrNull(PreferencesKeys.PUSH_TOKEN)
	}

	override suspend fun markAsSubscribed(token: String) {
		settings.putBoolean(PreferencesKeys.IS_SUBSCRIBED, true)
		settings.putString(PreferencesKeys.PUSH_TOKEN, token)
	}

	override suspend fun clearSubscription() {
		settings.remove(PreferencesKeys.IS_SUBSCRIBED)
		settings.remove(PreferencesKeys.PUSH_TOKEN)
	}
}
