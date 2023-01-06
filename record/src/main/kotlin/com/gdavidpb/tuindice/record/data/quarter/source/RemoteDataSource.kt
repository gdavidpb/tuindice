package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

interface RemoteDataSource {
	suspend fun getQuarters(): List<Quarter>
	suspend fun updateQuarter(qid: String, request: UpdateQuarterRequest): Quarter
	suspend fun removeQuarter(qid: String)
}