package com.gdavidpb.tuindice.persistence.data.source.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.source.room.entities.AccountEntity
import com.gdavidpb.tuindice.persistence.data.source.room.schema.AccountTable
import java.util.*

@Dao
interface AccountDao : BaseDao<AccountEntity> {
	@Query(
		"SELECT EXISTS(" +
				"SELECT 1 FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid " +
				"AND (${AccountTable.LAST_UPDATE} + 86400 * 1000) >= epochtime('now'))"
	)
	suspend fun isUpdated(uid: String): Boolean

	@Query(
		"SELECT EXISTS(" +
				"SELECT 1 FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid)"
	)
	suspend fun accountExists(uid: String): Boolean

	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	suspend fun getAccount(uid: String): AccountEntity
}