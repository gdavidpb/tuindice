package com.gdavidpb.tuindice.summary.data.room

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.room.mapper.toAccount
import com.gdavidpb.tuindice.summary.data.room.mapper.toAccountEntity
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase,
	private val sharedPreferences: SharedPreferences
) : LocalDataSource {
	override suspend fun isGetAccountOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_ACCOUNT, 0L)

		return cooldownTime <= System.currentTimeMillis()
	}

	override suspend fun setGetAccountCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_ACCOUNT

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_ACCOUNT, cooldownTime)
		}
	}

	override suspend fun getAccount(uid: String): Flow<Account?> {
		return room.accounts.getAccount(uid)
			.map { account -> account?.toAccount() }
	}

	override suspend fun saveAccount(uid: String, account: Account) {
		val accountEntity = account.toAccountEntity()

		room.accounts.upsertEntities(listOf(accountEntity))
	}

	override suspend fun saveProfilePicture(uid: String, url: String) {
		room.accounts.updateProfilePicture(uid, url)
	}
}