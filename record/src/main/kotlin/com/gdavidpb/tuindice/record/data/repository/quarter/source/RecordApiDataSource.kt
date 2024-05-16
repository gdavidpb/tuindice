package com.gdavidpb.tuindice.record.data.repository.quarter.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.record.data.repository.quarter.RecordApi
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toUpdateQuarterRequest
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate

class RecordApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): List<RemoteQuarter> {
		return recordApi.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		recordApi.deleteQuarter(remove.id)
			.getOrThrow()
	}

	override suspend fun updateQuarter(update: QuarterUpdate) {
		val request = update.toUpdateQuarterRequest()

		recordApi.updateQuarter(request)
			.getOrThrow()
	}
}