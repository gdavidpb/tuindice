package com.gdavidpb.tuindice.summary.data.repository.user


import com.gdavidpb.tuindice.base.domain.model.User
import kotlinx.coroutines.flow.Flow

interface LocalDataRepository {
	fun getUserFlow(): Flow<User?>
	suspend fun updateUser(user: User)
}
