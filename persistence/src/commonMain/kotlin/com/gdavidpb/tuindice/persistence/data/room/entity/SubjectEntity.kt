package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable

@Entity(
	tableName = SubjectTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = QuarterEntity::class,
			parentColumns = [QuarterTable.ID],
			childColumns = [SubjectTable.QUARTER_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = [SubjectTable.QUARTER_ID, SubjectTable.CODE], unique = true)
	]
)
data class SubjectEntity(
	@PrimaryKey @ColumnInfo(name = SubjectTable.ID) val id: String,
	@ColumnInfo(name = SubjectTable.QUARTER_ID) val quarterId: String,
	@ColumnInfo(name = SubjectTable.CODE) val code: String,
	@ColumnInfo(name = SubjectTable.NAME) val name: String,
	@ColumnInfo(name = SubjectTable.CREDITS) val credits: Int,
	@ColumnInfo(name = SubjectTable.GRADE) val grade: Int
)