package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.UserEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.UserTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : UpsertDao<UserEntity>() {
	@Query(
		"SELECT * FROM ${UserTable.TABLE_NAME}"
	)
	abstract fun getUserFlow(): Flow<UserEntity?>

	@Query(
		"UPDATE ${UserTable.TABLE_NAME} " +
				"SET ${UserTable.PICTURE_URL} = :url "
	)
	abstract suspend fun updateProfilePicture(
		url: String
	)

	@Query("DELETE FROM ${UserTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
