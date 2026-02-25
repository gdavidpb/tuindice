package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.Dao
import androidx.room.Query
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Dao
abstract class SubjectDao : UpsertDao<SubjectEntity>() {
	@Query(
		"SELECT * FROM ${SubjectTable.TABLE_NAME} " +
				"WHERE ${SubjectTable.ID} = :sid"
	)
	abstract suspend fun getSubject(
		sid: String
	): SubjectEntity

	@Query(
		"UPDATE ${SubjectTable.TABLE_NAME} " +
				"SET ${SubjectTable.GRADE} = :grade " +
				"WHERE ${SubjectTable.ID} = :sid"
	)
	abstract suspend fun updateSubject(
		sid: String,
		grade: Int
	)

	@Query(
		"DELETE FROM ${SubjectTable.TABLE_NAME} " +
				"WHERE ${SubjectTable.ID} = :sid"
	)
	abstract suspend fun deleteSubject(
		sid: String
	): Int
}