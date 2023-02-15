package com.gdavidpb.tuindice.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import java.util.*

class AuthMockDataSource(
	private val sharedPreferences: SharedPreferences
) : AuthRepository {

	private val uid = "0IQZf2qtYZOTwutEX38qvBY5H8l2"
	private val email = "11-11111@usb.ve"

	private val token = UUID.randomUUID().toString()

	override suspend fun isActiveAuth(): Boolean {
		return sharedPreferences.contains(PreferencesKeys.ACTIVE_TOKEN)
	}

	override suspend fun getActiveAuth(): Auth {
		return Auth(uid = uid, email = email)
	}

	override suspend fun signIn(token: String): Auth {
		sharedPreferences.edit {
			putString(PreferencesKeys.ACTIVE_TOKEN, token)
		}

		return Auth(uid = uid, email = email)
	}

	override suspend fun signOut() {
		sharedPreferences.edit {
			remove(PreferencesKeys.ACTIVE_TOKEN)
		}
	}

	override suspend fun getActiveToken(): String {
		return token
	}
}