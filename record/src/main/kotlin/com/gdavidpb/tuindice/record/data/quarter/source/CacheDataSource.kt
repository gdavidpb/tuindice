package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter

interface CacheDataSource {
	suspend fun computeQuarters(uid: String, origin: Quarter, quarters: List<Quarter>): List<Quarter>

	suspend fun invalidate(uid: String)
}