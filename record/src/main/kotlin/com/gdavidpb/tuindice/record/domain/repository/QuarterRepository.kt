package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate
import kotlinx.coroutines.flow.Flow

interface QuarterRepository {
	suspend fun getQuartersFlow(uid: String): Flow<List<Quarter>>
	suspend fun updateQuarter(uid: String, update: QuarterUpdate)
	suspend fun removeQuarter(uid: String, remove: QuarterRemove)
}