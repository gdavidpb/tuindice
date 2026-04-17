package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsGradeBinEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsGradeBinTable

@Dao
abstract class SubjectStatsGradeBinDao : UpsertDao<SubjectStatsGradeBinEntity>() {
	@Query(
		"SELECT * FROM ${SubjectStatsGradeBinTable.TABLE_NAME} " +
			"WHERE ${SubjectStatsGradeBinTable.SUBJECT_CODE} = :subjectCode"
	)
	abstract suspend fun getSubjectGradeBins(subjectCode: String): List<SubjectStatsGradeBinEntity>

	@Query("DELETE FROM ${SubjectStatsGradeBinTable.TABLE_NAME} WHERE ${SubjectStatsGradeBinTable.SUBJECT_CODE} = :subjectCode")
	abstract suspend fun deleteBySubjectCode(subjectCode: String): Int
}
