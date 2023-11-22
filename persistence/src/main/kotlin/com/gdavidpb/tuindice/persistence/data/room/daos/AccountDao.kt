package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AccountEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class AccountDao : UpsertDao<AccountEntity>() {
	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	abstract fun getAccountStream(
		uid: String
	): Flow<AccountEntity?>

	@Query(
		"UPDATE ${AccountTable.TABLE_NAME} " +
				"SET ${AccountTable.PICTURE_URL} = :url " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	abstract suspend fun updateProfilePicture(
		uid: String,
		url: String
	)
}