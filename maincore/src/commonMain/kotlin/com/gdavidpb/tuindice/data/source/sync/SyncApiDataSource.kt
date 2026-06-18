package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.data.source.sync.api.mapper.toSyncReport
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.api.mapper.toSyncResult
import com.gdavidpb.tuindice.data.source.sync.api.request.SyncRecordRequest
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncRecordErrorResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncRecordResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SyncApiDataSource(
	private val ktorClient: HttpClient
) : SyncRemoteDataRepository {
	override suspend fun sync(password: String): SyncResult {
		return try {
			ktorClient.post("record/v5/sync") {
				setBody(
					SyncRecordRequest(
						password = password
					)
				)
			}
				.body<SyncRecordResponse>()
				.toSyncResult()
		} catch (throwable: ResponseException) {
			throw SyncRemoteException(
				statusCode = throwable.response.status,
				syncReport = throwable.syncReportOrNull(),
				cause = throwable
			)
		}
	}

	private suspend fun ResponseException.syncReportOrNull() =
		runCatching {
			response.body<SyncRecordErrorResponse>().sync.toSyncReport()
		}
			.getOrNull()
}
