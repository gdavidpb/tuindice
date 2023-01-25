package com.gdavidpb.tuindice.data.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.SettingsKeys
import java.util.*

class AuthMockDataSource(
	private val sharedPreferences: SharedPreferences
) : AuthRepository {

	private val uid = "0IQZf2qtYZOTwutEX38qvBY5H8l2"
	private val email = "11-11111@usb.ve"

	private val token = UUID.randomUUID().toString()

	override suspend fun isActiveAuth(): Boolean {
		return sharedPreferences.contains(SettingsKeys.ACTIVE_TOKEN)
	}

	override suspend fun getActiveAuth(): Auth {
		return Auth(uid = uid, email = email)
	}

	override suspend fun signIn(token: String): Auth {
		sharedPreferences.edit {
			putString(SettingsKeys.ACTIVE_TOKEN, token)
		}

		return Auth(uid = uid, email = email)
	}

	override suspend fun signOut() {
		sharedPreferences.edit {
			remove(SettingsKeys.ACTIVE_TOKEN)
		}
	}

	override suspend fun getActiveToken(): String {
		return token
	}
}