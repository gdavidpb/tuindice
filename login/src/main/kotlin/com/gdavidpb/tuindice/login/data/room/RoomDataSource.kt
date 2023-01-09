package com.gdavidpb.tuindice.login.data.room

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.login.domain.repository.LocalRepository
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toAccount

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalRepository {
	override suspend fun getAccount(uid: String): Account {
		val accountEntity = room.accounts.getAccount(uid)

		return accountEntity.toAccount()
	}
}