package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.source.sync.api.request.SyncRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SyncApiDataSource(
	private val ktorClient: HttpClient
) : SyncRemoteDataRepository {
	override suspend fun sync(password: String) {
		ktorClient.post("record/v3/sync") {
			setBody(
				SyncRequest(
					password = password
				)
			)
		}
	}
}
