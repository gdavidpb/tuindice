package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumCacheTable

@Entity(
	tableName = PensumCacheTable.TABLE_NAME,
	indices = [
		Index(value = [PensumCacheTable.YEAR, PensumCacheTable.MODALITY_ID])
	]
)
data class PensumCacheEntity(
	@PrimaryKey
	@ColumnInfo(name = PensumCacheTable.CACHE_KEY) val cacheKey: String,
	@ColumnInfo(name = PensumCacheTable.YEAR) val year: Int,
	@ColumnInfo(name = PensumCacheTable.MODALITY_ID) val modalityId: String,
	@ColumnInfo(name = PensumCacheTable.PAYLOAD_JSON) val payloadJson: String,
	@ColumnInfo(name = PensumCacheTable.UPDATED_AT) val updatedAt: Long
)
