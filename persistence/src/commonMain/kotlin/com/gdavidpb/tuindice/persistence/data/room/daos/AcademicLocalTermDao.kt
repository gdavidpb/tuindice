package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicLocalTermEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicLocalTermTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicLocalTermDao : UpsertDao<AcademicLocalTermEntity>() {
	@Query("SELECT * FROM ${AcademicLocalTermTable.TABLE_NAME} WHERE ${AcademicLocalTermTable.RECORD_ID} = :recordId ORDER BY ${AcademicLocalTermTable.START_AT} DESC, ${AcademicLocalTermTable.ID} ASC")
	abstract fun observeTermsFlow(recordId: String): Flow<List<AcademicLocalTermEntity>>

	@Query("SELECT * FROM ${AcademicLocalTermTable.TABLE_NAME} WHERE ${AcademicLocalTermTable.RECORD_ID} = :recordId ORDER BY ${AcademicLocalTermTable.START_AT} DESC, ${AcademicLocalTermTable.ID} ASC")
	abstract suspend fun getTerms(recordId: String): List<AcademicLocalTermEntity>

	@Query("DELETE FROM ${AcademicLocalTermTable.TABLE_NAME} WHERE ${AcademicLocalTermTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)

	@Query("DELETE FROM ${AcademicLocalTermTable.TABLE_NAME} WHERE ${AcademicLocalTermTable.ID} = :termId")
	abstract suspend fun deleteById(termId: String)
}
