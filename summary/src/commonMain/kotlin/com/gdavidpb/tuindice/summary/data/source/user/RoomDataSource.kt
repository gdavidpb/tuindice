package com.gdavidpb.tuindice.summary.data.source.user

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.data.contract.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.source.user.database.mapper.toUser
import com.gdavidpb.tuindice.summary.data.source.user.database.mapper.toUserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override fun getUserFlow(): Flow<User?> {
		return room.users.getUserFlow()
			.map { userEntity -> userEntity?.toUser() }
	}

	override suspend fun updateUser(user: User) {
		val userEntity = user.toUserEntity()

		room.users.upsertEntity(userEntity)
	}
}
