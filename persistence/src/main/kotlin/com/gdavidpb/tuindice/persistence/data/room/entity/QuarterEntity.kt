package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import java.util.Date

@Entity(
	tableName = QuarterTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = AccountEntity::class,
			parentColumns = [AccountTable.ID],
			childColumns = [QuarterTable.ACCOUNT_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = [QuarterTable.START_DATE, QuarterTable.END_DATE], unique = true),
		Index(value = [QuarterTable.NAME], unique = true),
		Index(value = [QuarterTable.ACCOUNT_ID])
	]
)
data class QuarterEntity(
	@PrimaryKey @ColumnInfo(name = QuarterTable.ID) val id: String,
	@ColumnInfo(name = QuarterTable.ACCOUNT_ID) val accountId: String,
	@ColumnInfo(name = QuarterTable.NAME) val name: String,
	@ColumnInfo(name = QuarterTable.STATUS) val status: Int,
	@ColumnInfo(name = QuarterTable.START_DATE) val startDate: Long,
	@ColumnInfo(name = QuarterTable.END_DATE) val endDate: Long,
	@ColumnInfo(name = QuarterTable.GRADE) val grade: Double,
	@ColumnInfo(name = QuarterTable.GRADE_SUM) val gradeSum: Double,
	@ColumnInfo(name = QuarterTable.CREDITS) val credits: Int
)