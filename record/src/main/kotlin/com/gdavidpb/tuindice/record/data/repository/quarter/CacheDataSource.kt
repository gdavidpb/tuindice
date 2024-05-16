package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter

interface CacheDataSource {
	suspend fun computeQuarters(
		uid: String,
		origin: LocalQuarter,
		quarters: List<LocalQuarter>
	): List<LocalQuarter>

	suspend fun invalidate(uid: String)
}