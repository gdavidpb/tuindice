package com.gdavidpb.tuindice.data.repository.sync.source

import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataSource
import com.gdavidpb.tuindice.data.repository.sync.source.api.request.SyncRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SyncApiDataSource(
	private val ktorClient: HttpClient
) : SyncRemoteDataSource {
	override suspend fun sync(password: String) {
		ktorClient.post("sync/v1") {
			setBody(
				SyncRequest(
					password = password
				)
			)
		}
	}
}
