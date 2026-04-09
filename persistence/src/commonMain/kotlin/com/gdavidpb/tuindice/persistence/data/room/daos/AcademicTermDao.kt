package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermTable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AcademicTermDao : UpsertDao<AcademicTermEntity>() {
	@Query("SELECT * FROM ${AcademicTermTable.TABLE_NAME} ORDER BY ${AcademicTermTable.START_AT} DESC, ${AcademicTermTable.ID} ASC")
	abstract fun observeTermsFlow(): Flow<List<AcademicTermEntity>>

	@Query("SELECT * FROM ${AcademicTermTable.TABLE_NAME} ORDER BY ${AcademicTermTable.START_AT} DESC, ${AcademicTermTable.ID} ASC")
	abstract suspend fun getTerms(): List<AcademicTermEntity>

	@Query("DELETE FROM ${AcademicTermTable.TABLE_NAME}")
	abstract suspend fun deleteAll()
}
