package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAddQuarterRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.source.api.response.QuarterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class RecordApiDataSource(
	private val ktorClient: HttpClient
) : QuarterRemoteDataSource {
	override suspend fun getQuarters(): List<RemoteQuarter> {
		return ktorClient.get("quarters")
			.body<List<QuarterResponse>>()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}

	override suspend fun getQuarter(qid: String): RemoteQuarter {
		return ktorClient.get("quarters/$qid")
			.body<QuarterResponse>()
			.toRemoteQuarter()
	}

	override suspend fun removeQuarter(qid: String) {
		ktorClient.delete("quarters/$qid")
	}

	override suspend fun addQuarter(quarter: RemoteQuarter): List<RemoteQuarter> {
		val request = quarter.toAddQuarterRequest()

		return ktorClient.post("quarters") {
			setBody(request)
		}
			.body<List<QuarterResponse>>()
			.map { quarterResponse -> quarterResponse.toRemoteQuarter() }
	}
}
