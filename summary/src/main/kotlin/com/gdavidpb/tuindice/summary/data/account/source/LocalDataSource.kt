package com.gdavidpb.tuindice.summary.data.account.source

import com.gdavidpb.tuindice.base.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun isGetAccountOnCooldown(): Boolean
	suspend fun setGetAccountCooldown()

	suspend fun getAccount(uid: String): Flow<Account?>
	suspend fun saveAccount(uid: String, account: Account)

	suspend fun saveProfilePicture(uid: String, url: String)
}