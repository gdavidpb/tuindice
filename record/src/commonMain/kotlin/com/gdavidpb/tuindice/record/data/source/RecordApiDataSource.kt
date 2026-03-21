package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAddQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.response.SetSubjectGradeRequest
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteQuarterResponse
import com.gdavidpb.tuindice.record.data.source.api.response.QuarterResponse
import com.gdavidpb.tuindice.record.data.source.api.response.SetSubjectGradeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RecordApiDataSource(
	private val ktorClient: HttpClient
) : QuarterRemoteDataSource {
	override suspend fun getQuarters(): List<RemoteQuarter> {
		return ktorClient.get("quarters/v1")
			.body<List<QuarterResponse>>()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter {
		return ktorClient.get("quarters/v1/$qid")
			.body<QuarterResponse>()
			.toRemoteQuarter()
	}

	override suspend fun removeQuarter(
		qid: String,
		mutationId: String,
		expectedRevision: Long
	): RemoteDeleteQuarterAck {
		return ktorClient.delete("quarters/v1/$qid") {
			setBody(
				DeleteQuarterRequest(
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<DeleteQuarterResponse>()
			.toRemoteDeleteQuarterAck()
	}

	override suspend fun addQuarter(quarter: RemoteQuarter): List<RemoteQuarter> {
		val request = quarter.toAddQuarterRequest()

		return ktorClient.post("quarters/v1") {
			setBody(request)
		}
			.body<List<QuarterResponse>>()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}

	override suspend fun setSubjectGrade(
		qid: String,
		sid: String,
		grade: Int,
		mutationId: String,
		expectedRevision: Long
	): RemoteSetSubjectGradeAck {
		return ktorClient.patch("quarters/v1/$qid/subjects/$sid") {
			setBody(
				SetSubjectGradeRequest(
					grade = grade,
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<SetSubjectGradeResponse>()
			.toRemoteSetSubjectGradeAck()
	}
}
