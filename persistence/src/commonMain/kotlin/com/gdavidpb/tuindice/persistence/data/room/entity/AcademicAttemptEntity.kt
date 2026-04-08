package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptTable

@Entity(
	tableName = AcademicAttemptTable.TABLE_NAME,
	indices = [
		Index(value = [AcademicAttemptTable.RECORD_ID, AcademicAttemptTable.TERM_ID]),
		Index(value = [AcademicAttemptTable.RECORD_ID, AcademicAttemptTable.SUBJECT_CODE])
	]
)
data class AcademicAttemptEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicAttemptTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicAttemptTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicAttemptTable.TERM_ID)
	val termId: String,
	@ColumnInfo(name = AcademicAttemptTable.SUBJECT_CODE)
	val subjectCode: String,
	@ColumnInfo(name = AcademicAttemptTable.SUBJECT_NAME)
	val subjectName: String,
	@ColumnInfo(name = AcademicAttemptTable.CREDITS)
	val credits: Int,
	@ColumnInfo(name = AcademicAttemptTable.SEQUENCE_IN_TERM)
	val sequenceInTerm: Int,
	@ColumnInfo(name = AcademicAttemptTable.GRADING_MODE)
	val gradingMode: String,
	@ColumnInfo(name = AcademicAttemptTable.RAW_GRADE_TOKEN)
	val rawGradeToken: String,
	@ColumnInfo(name = AcademicAttemptTable.RAW_OBSERVATION_TEXT)
	val rawObservationText: String,
	@ColumnInfo(name = AcademicAttemptTable.SCORE_KIND)
	val scoreKind: String,
	@ColumnInfo(name = AcademicAttemptTable.SCORE_NUMERIC_VALUE)
	val scoreNumericValue: Int? = null,
	@ColumnInfo(name = AcademicAttemptTable.SCORE_SYMBOLIC_VALUE)
	val scoreSymbolicValue: String? = null,
	@ColumnInfo(name = AcademicAttemptTable.OFFICIAL_OUTCOME)
	val officialOutcome: String,
	@ColumnInfo(name = AcademicAttemptTable.OFFICIAL_BADGE)
	val officialBadge: String,
	@ColumnInfo(name = AcademicAttemptTable.EDITABLE)
	val editable: Boolean,
	@ColumnInfo(name = AcademicAttemptTable.SOURCE)
	val source: String,
	@ColumnInfo(name = AcademicAttemptTable.SYNTHETIC)
	val synthetic: Boolean
)
