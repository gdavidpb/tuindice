package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectDetailEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectDetailTable

@Dao
abstract class SubjectDetailDao : UpsertDao<SubjectDetailEntity>() {
	@Query(
		"SELECT * FROM ${SubjectDetailTable.TABLE_NAME} " +
			"WHERE ${SubjectDetailTable.SUBJECT_CODE} = :subjectCode"
	)
	abstract suspend fun getSubjectDetail(subjectCode: String): SubjectDetailEntity?

	@Query("DELETE FROM ${SubjectDetailTable.TABLE_NAME} WHERE ${SubjectDetailTable.SUBJECT_CODE} = :subjectCode")
	abstract suspend fun deleteBySubjectCode(subjectCode: String): Int
}
