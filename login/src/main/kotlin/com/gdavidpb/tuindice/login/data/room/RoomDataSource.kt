package com.gdavidpb.tuindice.login.data.room

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.login.domain.repository.LocalRepository

class RoomDataSource(
) : LocalRepository {
	override suspend fun getAccount(uid: String): Account {
		TODO("Not yet implemented")
	}
}