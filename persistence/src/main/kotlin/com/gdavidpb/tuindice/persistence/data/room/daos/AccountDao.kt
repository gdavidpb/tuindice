package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AccountEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class AccountDao {
	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid " +
				"LIMIT 1"
	)
	abstract fun getAccount(
		uid: String
	): Flow<AccountEntity?>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	abstract suspend fun insertAccount(
		account: AccountEntity
	)

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