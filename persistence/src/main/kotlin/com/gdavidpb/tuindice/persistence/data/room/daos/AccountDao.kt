package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AccountEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AccountDao : UpsertDao<AccountEntity>() {
	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME}"
	)
	abstract fun getAccountFlow(): Flow<AccountEntity?>

	@Query(
		"UPDATE ${AccountTable.TABLE_NAME} " +
				"SET ${AccountTable.PICTURE_URL} = :url "
	)
	abstract suspend fun updateProfilePicture(
		url: String
	)
}