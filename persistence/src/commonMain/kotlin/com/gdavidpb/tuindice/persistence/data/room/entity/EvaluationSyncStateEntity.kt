package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationSyncStateTable

@Entity(tableName = EvaluationSyncStateTable.TABLE_NAME)
data class EvaluationSyncStateEntity(
	@PrimaryKey
	@ColumnInfo(name = EvaluationSyncStateTable.KEY)
	val key: String = EvaluationSyncStateTable.DEFAULT_KEY,
	@ColumnInfo(name = EvaluationSyncStateTable.ANCHOR_REVISION)
	val anchorRevision: Long
)
