package com.gdavidpb.tuindice.data.source.room.entities

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.gdavidpb.tuindice.data.source.room.schema.AccountTable
import com.gdavidpb.tuindice.data.source.room.schema.QuarterTable
import java.util.*

@Entity(
	tableName = QuarterTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = AccountEntity::class,
			parentColumns = [AccountTable.ID],
			childColumns = [QuarterTable.ACCOUNT_ID],
			onDelete = CASCADE,
			onUpdate = CASCADE
		)
	],
	indices = [
		Index(value = [QuarterTable.START_DATE, QuarterTable.END_DATE], unique = true),
		Index(value = [QuarterTable.ACCOUNT_ID])
	]
)
data class QuarterEntity(
	@PrimaryKey @ColumnInfo(name = QuarterTable.ID) val id: String,
	@ColumnInfo(name = QuarterTable.ACCOUNT_ID) val accountId: String,
	@ColumnInfo(name = QuarterTable.STATUS) val status: Int,
	@ColumnInfo(name = QuarterTable.START_DATE) val startDate: Date,
	@ColumnInfo(name = QuarterTable.END_DATE) val endDate: Date,
	@ColumnInfo(name = QuarterTable.GRADE) val grade: Double,
	@ColumnInfo(name = QuarterTable.GRADE_SUM) val gradeSum: Double,
	@ColumnInfo(name = QuarterTable.CREDITS) val credits: Int
)