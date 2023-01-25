package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.*
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Entity(
	tableName = SubjectTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = AccountEntity::class,
			parentColumns = [AccountTable.ID],
			childColumns = [SubjectTable.ACCOUNT_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = QuarterEntity::class,
			parentColumns = [QuarterTable.ID],
			childColumns = [SubjectTable.QUARTER_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = [SubjectTable.QUARTER_ID, SubjectTable.CODE], unique = true),
		Index(value = [SubjectTable.ACCOUNT_ID])
	]
)
data class SubjectEntity(
	@PrimaryKey @ColumnInfo(name = SubjectTable.ID) val id: String,
	@ColumnInfo(name = SubjectTable.QUARTER_ID) val quarterId: String,
	@ColumnInfo(name = SubjectTable.ACCOUNT_ID) val accountId: String,
	@ColumnInfo(name = SubjectTable.CODE) val code: String,
	@ColumnInfo(name = SubjectTable.NAME) val name: String,
	@ColumnInfo(name = SubjectTable.CREDITS) val credits: Int,
	@ColumnInfo(name = SubjectTable.GRADE) val grade: Int,
	@ColumnInfo(name = SubjectTable.STATUS) val status: Int
)