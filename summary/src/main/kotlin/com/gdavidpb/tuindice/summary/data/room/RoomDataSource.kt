package com.gdavidpb.tuindice.summary.data.room

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toAccount
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toAccountEntity
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun accountExists(uid: String): Boolean {
		return room.accounts.accountExists(uid)
	}

	override suspend fun getAccount(uid: String): Account {
		return room.accounts.getAccount(uid).toAccount()
	}

	override suspend fun saveAccount(uid: String, account: Account) {
		val accountEntity = account.toAccountEntity(uid)

		room.accounts.insert(accountEntity)
	}

	override suspend fun saveProfilePicture(uid: String, url: String) {
		room.accounts.updateProfilePicture(uid, url)
	}
}