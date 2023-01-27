package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Dao
interface SubjectDao {
	@Query(
		"SELECT * FROM ${SubjectTable.TABLE_NAME} " +
				"WHERE ${SubjectTable.ACCOUNT_ID} = :uid " +
				"AND ${SubjectTable.ID} = :sid"
	)
	suspend fun getSubject(
		uid: String,
		sid: String
	): SubjectEntity

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSubjects(
		subjects: List<SubjectEntity>
	)

	@Query(
		"UPDATE ${SubjectTable.TABLE_NAME} " +
				"SET ${SubjectTable.GRADE} = :grade " +
				"WHERE ${SubjectTable.ACCOUNT_ID} = :uid " +
				"AND ${SubjectTable.ID} = :sid"
	)
	suspend fun updateSubject(
		uid: String,
		sid: String,
		grade: Int
	)
}