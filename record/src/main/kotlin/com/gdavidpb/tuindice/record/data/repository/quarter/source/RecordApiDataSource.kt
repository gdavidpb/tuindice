package com.gdavidpb.tuindice.record.data.repository.quarter.source

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.record.data.repository.quarter.RecordApi
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toUpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

class RecordApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): List<Quarter> {
		return recordApi.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toQuarter() }
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		recordApi.deleteQuarter(remove.quarterId)
			.getOrThrow()
	}

	override suspend fun updateSubject(update: SubjectUpdate): Subject {
		val request = update.toUpdateSubjectRequest()

		return recordApi.updateSubject(request)
			.getOrThrow()
			.toSubject(isEditable = true)
	}
}