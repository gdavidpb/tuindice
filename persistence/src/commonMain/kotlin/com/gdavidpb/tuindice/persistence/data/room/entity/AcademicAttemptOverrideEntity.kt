package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptOverrideTable

@Entity(
	tableName = AcademicAttemptOverrideTable.TABLE_NAME,
	indices = [Index(value = [AcademicAttemptOverrideTable.UPDATED_AT])]
)
data class AcademicAttemptOverrideEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicAttemptOverrideTable.ATTEMPT_ID)
	val attemptId: String,
	@ColumnInfo(name = AcademicAttemptOverrideTable.SCORE_KIND)
	val scoreKind: String? = null,
	@ColumnInfo(name = AcademicAttemptOverrideTable.SCORE_NUMERIC_VALUE)
	val scoreNumericValue: Int? = null,
	@ColumnInfo(name = AcademicAttemptOverrideTable.SCORE_SYMBOLIC_VALUE)
	val scoreSymbolicValue: String? = null,
	@ColumnInfo(name = AcademicAttemptOverrideTable.OUTCOME)
	val outcome: String? = null,
	@ColumnInfo(name = AcademicAttemptOverrideTable.UPDATED_AT)
	val updatedAt: Long
)
