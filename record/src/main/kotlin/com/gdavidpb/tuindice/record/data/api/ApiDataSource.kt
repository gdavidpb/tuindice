package com.gdavidpb.tuindice.record.data.api

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.record.data.api.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.api.mapper.toSubject
import com.gdavidpb.tuindice.record.data.api.mapper.toUpdateSubjectRequest
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource

class ApiDataSource(
	private val recordApi: RecordApi
) : RemoteDataSource {
	override suspend fun getQuarters(): List<Quarter> {
		return recordApi.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toQuarter() }
	}

	override suspend fun removeQuarter(transaction: Transaction<QuarterRemoveTransaction>) {
		recordApi.deleteQuarter(quarterId = transaction.operation.quarterId)
			.getOrThrow()
	}

	override suspend fun updateSubject(transaction: Transaction<SubjectUpdateTransaction>): Subject {
		val request = transaction.toUpdateSubjectRequest()

		return recordApi.updateSubject(request)
			.getOrThrow()
			.toSubject()
	}
}