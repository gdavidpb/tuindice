package com.gdavidpb.tuindice.summary.data.repository.account.source

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.data.repository.account.LocalDataSource
import com.gdavidpb.tuindice.summary.data.repository.account.source.database.mapper.toAccount
import com.gdavidpb.tuindice.summary.data.repository.account.source.database.mapper.toAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override fun getAccountFlow(): Flow<Account?> {
		return room.accounts.getAccountFlow()
			.map { account -> account?.toAccount() }
	}

	override suspend fun saveAccount(account: Account) {
		val accountEntity = account.toAccountEntity()

		room.accounts.upsertEntities(listOf(accountEntity))
	}

	override suspend fun saveProfilePicture(url: String) {
		room.accounts.updateProfilePicture(url)
	}
}