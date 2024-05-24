package com.gdavidpb.tuindice.record.data.repository.quarter.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.record.data.repository.quarter.RecordApi
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toUpdateQuarterRequest

class RecordApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): List<RemoteQuarter> {
		return recordApi.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter? {
		return recordApi.getQuarter()
			.getOrThrow()
			.toRemoteQuarter()
	}

	override suspend fun removeQuarter(qid: String) {
		recordApi.deleteQuarter(qid)
			.getOrThrow()
	}

	override suspend fun updateQuarter(quarter: RemoteQuarter): RemoteQuarter {
		val request = quarter.toUpdateQuarterRequest()

		return recordApi.updateQuarter(request)
			.getOrThrow()
			.toRemoteQuarter()
	}
}