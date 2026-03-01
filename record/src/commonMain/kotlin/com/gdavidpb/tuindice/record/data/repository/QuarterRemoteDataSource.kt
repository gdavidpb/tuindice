package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter

interface QuarterRemoteDataSource {
	suspend fun getQuarters(): List<RemoteQuarter>
	suspend fun getQuarter(qid: String): RemoteQuarter
	suspend fun removeQuarter(qid: String)
	suspend fun addQuarter(quarter: RemoteQuarter): List<RemoteQuarter>
}
