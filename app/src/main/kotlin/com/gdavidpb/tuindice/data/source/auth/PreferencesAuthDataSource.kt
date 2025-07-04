package com.gdavidpb.tuindice.data.source.auth

import android.content.SharedPreferences
import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys

class PreferencesAuthDataSource(
	private val sharedPreferences: SharedPreferences
) : AuthRepository {
	override suspend fun isActiveAuth(): Boolean {
		return sharedPreferences.contains(PreferencesKeys.USER_UID)
	}

	override suspend fun getActiveAuth(): Auth? {
		return if (isActiveAuth())
			Auth(
				uid =
					sharedPreferences.getString(
						PreferencesKeys.USER_UID,
						null
					)!!,
				email =
					sharedPreferences.getString(
						PreferencesKeys.USER_EMAIL,
						null
					)!!,
				accessToken = sharedPreferences.getString(
					PreferencesKeys.USER_ACCESS_TOKEN,
					null
				)!!,
				refreshToken = sharedPreferences.getString(
					PreferencesKeys.USER_REFRESH_TOKEN,
					null
				)!!
			)
		else
			null
	}
}