package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable

@Entity(
	tableName = EvaluationTable.TABLE_NAME,
	indices = [
		Index(value = [EvaluationTable.TERM_ID]),
		Index(value = [EvaluationTable.ATTEMPT_ID])
	]
)
data class EvaluationEntity(
	@PrimaryKey @ColumnInfo(name = EvaluationTable.ID) val id: String,
	@ColumnInfo(name = EvaluationTable.REFERENCE_ID) val referenceId: String,
	@ColumnInfo(name = EvaluationTable.ATTEMPT_ID) val attemptId: String,
	@ColumnInfo(name = EvaluationTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = EvaluationTable.TERM_ID) val termId: String,
	@ColumnInfo(name = EvaluationTable.REVISION) val revision: Long,
	@ColumnInfo(name = EvaluationTable.SCHEDULE_MODE) val scheduleMode: EvaluationScheduleMode,
	@ColumnInfo(name = EvaluationTable.GRADE) val grade: Double?,
	@ColumnInfo(name = EvaluationTable.MAX_GRADE) val maxGrade: Double,
	@ColumnInfo(name = EvaluationTable.DATE) val date: Long?,
	@ColumnInfo(name = EvaluationTable.TYPE) val type: Int,
	@ColumnInfo(name = EvaluationTable.IS_DONE) val isDone: Boolean
)
