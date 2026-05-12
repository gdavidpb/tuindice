package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SyntheticTermLoadPreviewCacheTable

@Entity(
	tableName = SyntheticTermLoadPreviewCacheTable.TABLE_NAME,
	indices = [
		Index(
			value = [
				SyntheticTermLoadPreviewCacheTable.TERM_KEY,
				SyntheticTermLoadPreviewCacheTable.SUBJECT_CODES_KEY
			]
		),
		Index(value = [SyntheticTermLoadPreviewCacheTable.EXPIRES_AT])
	]
)
data class SyntheticTermLoadPreviewCacheEntity(
	@PrimaryKey
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.CACHE_KEY) val cacheKey: String,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.TERM_KEY) val termKey: String,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.SUBJECT_CODES_KEY) val subjectCodesKey: String,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.AVAILABLE) val available: Boolean,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.REASON) val reason: String?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.BAND) val band: String?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.CREDITS) val credits: Int?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.WEIGHTED_DIFFICULTY) val weightedDifficulty: Double?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.LOAD_INDEX) val loadIndex: Double?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.BASELINE_LOAD_INDEX) val baselineLoadIndex: Double?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.EFFECTIVE_TERMS) val effectiveTerms: Int?,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.UPDATED_AT) val updatedAt: Long,
	@ColumnInfo(name = SyntheticTermLoadPreviewCacheTable.EXPIRES_AT) val expiresAt: Long
)
