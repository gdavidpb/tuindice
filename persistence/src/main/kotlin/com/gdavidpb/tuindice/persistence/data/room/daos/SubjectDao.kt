package com.gdavidpb.tuindice.persistence.data.room.daos

import androidx.room.*
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectEntity
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Dao
abstract class SubjectDao : UpsertDao<SubjectEntity>() {
	@Query(
		"SELECT * FROM ${SubjectTable.TABLE_NAME} " +
				"WHERE ${SubjectTable.ACCOUNT_ID} = :uid " +
				"AND ${SubjectTable.ID} = :sid"
	)
	abstract suspend fun getSubject(
		uid: String,
		sid: String
	): SubjectEntity

	@Query(
		"UPDATE ${SubjectTable.TABLE_NAME} " +
				"SET ${SubjectTable.GRADE} = :grade " +
				"WHERE ${SubjectTable.ACCOUNT_ID} = :uid " +
				"AND ${SubjectTable.ID} = :sid"
	)
	abstract suspend fun updateSubject(
		uid: String,
		sid: String,
		grade: Int
	)
}