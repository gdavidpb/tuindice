package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.data.source.room.entities.AccountEntity
import com.gdavidpb.tuindice.data.source.room.schema.AccountTable
import java.util.Date

@Dao
interface AccountDao : BaseDao<AccountEntity> {
	@Query(
		"SELECT * FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	suspend fun getAccount(uid: String): AccountEntity

	@Query(
		"SELECT ${AccountTable.LAST_UPDATE} FROM ${AccountTable.TABLE_NAME} " +
				"WHERE ${AccountTable.ID} = :uid"
	)
	suspend fun getLastUpdate(uid: String): Date
}