package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate

interface RemoteDataSource {
	suspend fun getQuarters(): List<RemoteQuarter>
	suspend fun removeQuarter(remove: QuarterRemove)

	suspend fun updateQuarter(update: QuarterUpdate)
}