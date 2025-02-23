package com.gdavidpb.tuindice.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.mapper.toDestinationName
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import java.util.UUID

class AuthMockDataSource(
	private val sharedPreferences: SharedPreferences
) : AuthRepository {

	private val uid = "0IQZf2qtYZOTwutEX38qvBY5H8l2"
	private val email = "11-11111@usb.ve"

	private val token = UUID.randomUUID().toString()

	override suspend fun isActiveAuth(): Boolean {
		return sharedPreferences.contains(PreferencesKeys.LAST_DESTINATION)
	}

	override suspend fun getActiveAuth(): Auth {
		return if (sharedPreferences.contains(PreferencesKeys.LAST_DESTINATION))
			Auth(uid = uid, email = email)
		else
			throw IllegalStateException()
	}

	override suspend fun auth(token: String): Auth {
		sharedPreferences.edit {
			putString(
				PreferencesKeys.LAST_DESTINATION,
				SummaryDestination.NavGraph.toDestinationName()
			)
		}

		return Auth(uid = uid, email = email)
	}

	override suspend fun revoke() {
		sharedPreferences.edit {
			remove(PreferencesKeys.LAST_DESTINATION)
		}
	}

	override suspend fun getActiveToken(): String {
		return if (sharedPreferences.contains(PreferencesKeys.LAST_DESTINATION))
			token
		else
			throw IllegalStateException()
	}
}