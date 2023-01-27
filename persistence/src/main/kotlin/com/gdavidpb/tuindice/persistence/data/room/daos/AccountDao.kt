package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AccountEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import java.util.*

@Dao
interface AccountDao : BaseDao<AccountEntity> {
	@Query(
		"SELECT EXISTS(" +
				"SELECT 1 FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid " +
				"AND datetime(${AccountTable.LAST_UPDATE} / 1000, 'unixepoch', '+1 day') >= datetime('now'))"
	)
	suspend fun isUpdated(uid: String): Boolean

	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	suspend fun getAccount(uid: String): AccountEntity

	@Query(
		"UPDATE ${AccountTable.TABLE_NAME} " +
				"SET ${AccountTable.PICTURE_URL} = :url " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	suspend fun updateProfilePicture(uid: String, url: String)
}