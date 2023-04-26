package com.gdavidpb.tuindice.summary.data.room

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.room.mapper.toAccount
import com.gdavidpb.tuindice.summary.data.room.mapper.toAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
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