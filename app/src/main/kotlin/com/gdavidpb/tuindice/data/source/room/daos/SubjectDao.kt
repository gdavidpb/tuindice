package com.gdavidpb.tuindice.data.source.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.data.source.room.entities.SubjectEntity
import com.gdavidpb.tuindice.data.source.room.schema.SubjectTable

@Dao
interface SubjectDao : BaseDao<SubjectEntity> {
	@Query("SELECT * FROM ${SubjectTable.TABLE_NAME} WHERE ${SubjectTable.QUARTER_ID} = :qid")
	suspend fun getSubjectsFromQuarter(qid: String): List<SubjectEntity>
}