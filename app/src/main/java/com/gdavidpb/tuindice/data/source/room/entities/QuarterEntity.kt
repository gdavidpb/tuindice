package com.gdavidpb.tuindice.data.source.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.data.source.room.utils.QuarterTable
import java.util.*

@Entity(
	tableName = "quarters",
	/*
	foreignKeys = [(ForeignKey(
			entity = AccountEntity::class,
			parentColumns = [COLUMN_ID],
			childColumns = [COLUMN_AID],
			onDelete = CASCADE,
			onUpdate = CASCADE))],*/
	indices = [
		Index(value = [QuarterTable.START_DATE, QuarterTable.END_DATE], unique = true)//,
		/*Index(value = [COLUMN_AID])*/
	]
)
data class QuarterEntity(
	@PrimaryKey
	val id: String,
	val aid: String,
	val status: Int,
	val startDate: Date,
	val endDate: Date,
	val grade: Double,
	val gradeSum: Double,
	val credits: Int
)