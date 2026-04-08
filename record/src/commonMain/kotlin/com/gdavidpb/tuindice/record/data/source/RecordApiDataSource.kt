package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataRepository
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAddQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.response.AddQuarterResponse
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
) : QuarterRemoteDataRepository {
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

	override suspend fun addQuarter(
		add: RecordMutation.AddQuarter,
		mutationId: String,
		expectedRevision: Long
	): RemoteAddQuarterAck {
		val request = add.toAddQuarterRequest(
			mutationId = mutationId,
			expectedRevision = expectedRevision
		)

		return ktorClient.post("quarters/v1") {
			setBody(request)
		}
			.body<AddQuarterResponse>()
			.toRemoteAddQuarterAck()
	}

	override suspend fun setSubjectGrade(
		qid: String,
		sid: String,
		grade: Int?,
		status: SubjectStatus?,
		mutationId: String,
		expectedRevision: Long
	): RemoteSetSubjectGradeAck {
		return ktorClient.patch("quarters/v1/$qid/subjects/$sid") {
			setBody(
				SetSubjectGradeRequest(
					grade = grade,
					status = status,
					mutationId = mutationId,
					expectedRevision = expectedRevision
				)
			)
		}
			.body<SetSubjectGradeResponse>()
			.toRemoteSetSubjectGradeAck()
	}
}
