package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.record.data.api.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource

class ApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): List<Quarter> {
		return recordApi.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toQuarter() }
	}

	override suspend fun removeQuarter(qid: String) {
		recordApi.deleteQuarter(qid)
			.getOrThrow()
	}
}