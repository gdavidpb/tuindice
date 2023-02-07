package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.record.data.api.mapper.toGetQuarters
import com.gdavidpb.tuindice.record.data.api.mapper.toSubject
import com.gdavidpb.tuindice.record.data.api.mapper.toUpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.Quarters
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

class ApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): Quarters {
		return recordApi.getQuarters()
			.getOrThrow()
			.toGetQuarters()
	}

	override suspend fun removeQuarter(qid: String) {
		recordApi.deleteQuarter(qid)
			.getOrThrow()
	}

	override suspend fun updateSubject(update: SubjectUpdate): Subject {
		val request = update.toUpdateSubjectRequest()

		return recordApi.updateSubject(request)
			.getOrThrow()
			.toSubject()
	}
}