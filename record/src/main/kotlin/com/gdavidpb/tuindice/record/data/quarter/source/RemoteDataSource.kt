package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter

interface RemoteDataSource {
	suspend fun getQuarters(): List<Quarter>
	suspend fun removeQuarter(qid: String)
}