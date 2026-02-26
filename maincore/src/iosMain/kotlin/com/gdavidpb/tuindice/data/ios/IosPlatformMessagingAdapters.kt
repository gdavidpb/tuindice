package com.gdavidpb.tuindice.data.ios

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.di.IosPlatformBridge
import com.gdavidpb.tuindice.login.data.model.SubscribeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.first

internal class IosMessagingDataRepository(
	private val dataStore: DataStore<Preferences>,
	private val httpClientProvider: () -> HttpClient,
	private val bridge: IosPlatformBridge
) : MessagingRepository {
	override suspend fun subscribe() {
		val httpClient = httpClientProvider()
		val token = bridge.pushToken()
			?.takeIf { value -> value.isNotBlank() }
			?: return
		val preferences = dataStore.data.first()
		val hasMatchingToken = preferences[IS_SUBSCRIBED] == true &&
			preferences[PUSH_TOKEN] == token

		if (hasMatchingToken) return

		httpClient.post("messaging") {
			setBody(SubscribeRequest(token))
		}

		dataStore.edit { preferences ->
			preferences[IS_SUBSCRIBED] = true
			preferences[PUSH_TOKEN] = token
		}
	}

	override suspend fun unsubscribe() {
		val httpClient = httpClientProvider()
		runCatching {
			httpClient.delete("messaging")
		}

		dataStore.edit { preferences ->
			preferences.remove(IS_SUBSCRIBED)
			preferences.remove(PUSH_TOKEN)
		}
	}

	companion object {
		private val IS_SUBSCRIBED = booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)
		private val PUSH_TOKEN = stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)
	}
}
