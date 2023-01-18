package com.gdavidpb.tuindice.persistence.data.source.room.entities

import androidx.room.*
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.persistence.data.source.room.schema.AccountTable
import com.gdavidpb.tuindice.persistence.data.source.room.schema.EvaluationTable
import com.gdavidpb.tuindice.persistence.data.source.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.source.room.schema.SubjectTable
import java.util.*

@Entity(
	tableName = EvaluationTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = AccountEntity::class,
			parentColumns = [AccountTable.ID],
			childColumns = [EvaluationTable.ACCOUNT_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = QuarterEntity::class,
			parentColumns = [QuarterTable.ID],
			childColumns = [EvaluationTable.QUARTER_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = [SubjectTable.ID],
			childColumns = [EvaluationTable.SUBJECT_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = [EvaluationTable.ACCOUNT_ID]),
		Index(value = [EvaluationTable.QUARTER_ID]),
		Index(value = [EvaluationTable.SUBJECT_ID])
	]
)
data class EvaluationEntity(
	@PrimaryKey @ColumnInfo(name = EvaluationTable.ID) val id: String,
	@ColumnInfo(name = EvaluationTable.SUBJECT_ID) val subjectId: String,
	@ColumnInfo(name = EvaluationTable.QUARTER_ID) val quarterId: String,
	@ColumnInfo(name = EvaluationTable.ACCOUNT_ID) val accountId: String,
	@ColumnInfo(name = EvaluationTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = EvaluationTable.NAME) val name: String,
	@ColumnInfo(name = EvaluationTable.GRADE) val grade: Double,
	@ColumnInfo(name = EvaluationTable.MAX_GRADE) val maxGrade: Double,
	@ColumnInfo(name = EvaluationTable.DATE) val date: Date,
	@ColumnInfo(name = EvaluationTable.LAST_MODIFIED) val lastModified: Date,
	@ColumnInfo(name = EvaluationTable.IS_DONE) val isDone: Boolean,
	@ColumnInfo(name = EvaluationTable.TYPE) val type: EvaluationType
)
