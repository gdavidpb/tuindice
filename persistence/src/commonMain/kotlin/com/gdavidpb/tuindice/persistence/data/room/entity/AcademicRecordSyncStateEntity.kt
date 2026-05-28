package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicRecordSyncStateTable

@Entity(tableName = AcademicRecordSyncStateTable.TABLE_NAME)
data class AcademicRecordSyncStateEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicRecordSyncStateTable.KEY)
	val key: String = AcademicRecordSyncStateTable.DEFAULT_KEY,
	@ColumnInfo(name = AcademicRecordSyncStateTable.HAS_SYNCED)
	val hasSynced: Boolean
)
