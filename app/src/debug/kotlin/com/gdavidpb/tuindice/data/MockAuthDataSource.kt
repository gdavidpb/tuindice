package com.gdavidpb.tuindice.data

import android.content.SharedPreferences
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MockAuthDataSource(
	private val sharedPreferences: SharedPreferences
) : AuthRepository {

	private val uid = "0IQZf2qtYZOTwutEX38qvBY5H8l2"
	private val email = "11-11111@usb.ve"
	private val accessToken = Uuid.random().toString()
	private val refreshToken = Uuid.random().toString()

	override suspend fun isActiveAuth(): Boolean {
		return sharedPreferences.contains(PreferencesKeys.LAST_DESTINATION)
	}

	override suspend fun getActiveAuth(): Auth? {
		return if (sharedPreferences.contains(PreferencesKeys.LAST_DESTINATION))
			Auth(
				uid = uid,
				email = email,
				accessToken = accessToken,
				refreshToken = refreshToken
			)
		else
			null
	}
}