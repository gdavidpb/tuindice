package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.mapper.toUser
import com.gdavidpb.tuindice.summary.data.mapper.toUserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	private val userDao: UserDao
) : LocalDataRepository {
	override fun getUserFlow(): Flow<User?> {
		return userDao.getUserFlow()
			.map { userEntity -> userEntity?.toUser() }
	}

	override suspend fun updateUser(user: User) {
		val userEntity = user.toUserEntity()

		userDao.upsertEntity(userEntity)
	}
}
