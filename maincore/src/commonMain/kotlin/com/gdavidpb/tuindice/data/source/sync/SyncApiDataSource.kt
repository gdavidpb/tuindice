package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.api.mapper.toSyncResult
import com.gdavidpb.tuindice.data.source.sync.api.request.SyncRequest
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SyncApiDataSource(
	private val ktorClient: HttpClient
) : SyncRemoteDataRepository {
	override suspend fun sync(password: String): SyncResult {
		return ktorClient.post("record/v5/sync") {
			setBody(
				SyncRequest(
					password = password
				)
			)
		}
			.body<SyncResponse>()
			.toSyncResult()
	}
}
