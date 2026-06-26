package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable

@Entity(tableName = PensumSelectionTable.TABLE_NAME)
data class PensumSelectionEntity(
	@PrimaryKey
	@ColumnInfo(name = PensumSelectionTable.ID) val id: String = PensumSelectionTable.DEFAULT_ID,
	@ColumnInfo(name = PensumSelectionTable.YEAR) val year: Int? = null,
	@ColumnInfo(name = PensumSelectionTable.MODALITY_ID) val modalityId: String? = null,
	@ColumnInfo(name = PensumSelectionTable.INFERRED) val inferred: Boolean = false,
	@ColumnInfo(name = PensumSelectionTable.CACHE_KEY) val cacheKey: String? = null,
	@ColumnInfo(name = PensumSelectionTable.UPDATED_AT) val updatedAt: Long
)
