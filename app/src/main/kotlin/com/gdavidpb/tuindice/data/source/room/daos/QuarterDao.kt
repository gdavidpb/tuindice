package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.data.source.room.entities.QuarterEntity
import com.gdavidpb.tuindice.data.source.room.schema.QuarterTable

@Dao
interface QuarterDao : BaseDao<QuarterEntity> {
	@Query("SELECT * FROM ${QuarterTable.TABLE_NAME} WHERE ${QuarterTable.ACCOUNT_ID} = :uid AND ${QuarterTable.ID} = :qid")
	suspend fun getQuarter(uid: String, qid: String): QuarterEntity
}