package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsAttemptBinEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsAttemptBinTable

@Dao
abstract class SubjectStatsAttemptBinDao : UpsertDao<SubjectStatsAttemptBinEntity>() {
	@Query(
		"SELECT * FROM ${SubjectStatsAttemptBinTable.TABLE_NAME} " +
			"WHERE ${SubjectStatsAttemptBinTable.SUBJECT_CODE} = :subjectCode"
	)
	abstract suspend fun getSubjectAttemptBins(subjectCode: String): List<SubjectStatsAttemptBinEntity>

	@Query("DELETE FROM ${SubjectStatsAttemptBinTable.TABLE_NAME} WHERE ${SubjectStatsAttemptBinTable.SUBJECT_CODE} = :subjectCode")
	abstract suspend fun deleteBySubjectCode(subjectCode: String): Int

	@Query("DELETE FROM ${SubjectStatsAttemptBinTable.TABLE_NAME}")
	abstract suspend fun deleteAll(): Int
}
