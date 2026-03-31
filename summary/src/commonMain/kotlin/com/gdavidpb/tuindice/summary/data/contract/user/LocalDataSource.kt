package com.gdavidpb.tuindice.summary.data.contract.user


import com.gdavidpb.tuindice.base.domain.model.User
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	fun getUserFlow(): Flow<User?>
	suspend fun updateUser(user: User)
}
