package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.database.mapper.toUserEntity
import com.gdavidpb.tuindice.summary.data.repository.user.source.database.mapper.toUser
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

		room.users.upsertEntities(listOf(userEntity))
	}
}
