package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsSegmentEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsSegmentTable

@Dao
abstract class SubjectStatsSegmentDao : UpsertDao<SubjectStatsSegmentEntity>() {
	@Query(
		"SELECT * FROM ${SubjectStatsSegmentTable.TABLE_NAME} " +
			"WHERE ${SubjectStatsSegmentTable.SUBJECT_CODE} = :subjectCode"
	)
	abstract suspend fun getSubjectSegments(subjectCode: String): List<SubjectStatsSegmentEntity>

	@Query("DELETE FROM ${SubjectStatsSegmentTable.TABLE_NAME} WHERE ${SubjectStatsSegmentTable.SUBJECT_CODE} = :subjectCode")
	abstract suspend fun deleteBySubjectCode(subjectCode: String): Int

	@Query("DELETE FROM ${SubjectStatsSegmentTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
