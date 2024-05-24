package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter

interface RemoteDataSource {
	suspend fun getQuarters(): List<RemoteQuarter>
	suspend fun getQuarter(qid: String): RemoteQuarter
	suspend fun removeQuarter(qid: String)
	suspend fun addQuarters(quarters: List<RemoteQuarter>): List<RemoteQuarter>
}