package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicTermDao : UpsertDao<AcademicTermEntity>() {
	@Query("SELECT * FROM ${AcademicTermTable.TABLE_NAME} WHERE ${AcademicTermTable.RECORD_ID} = :recordId ORDER BY ${AcademicTermTable.START_AT} DESC, ${AcademicTermTable.ID} ASC")
	abstract fun observeTermsFlow(recordId: String): Flow<List<AcademicTermEntity>>

	@Query("SELECT * FROM ${AcademicTermTable.TABLE_NAME} WHERE ${AcademicTermTable.RECORD_ID} = :recordId ORDER BY ${AcademicTermTable.START_AT} DESC, ${AcademicTermTable.ID} ASC")
	abstract suspend fun getTerms(recordId: String): List<AcademicTermEntity>

	@Query("DELETE FROM ${AcademicTermTable.TABLE_NAME} WHERE ${AcademicTermTable.RECORD_ID} = :recordId")
	abstract suspend fun deleteByRecord(recordId: String)
}
