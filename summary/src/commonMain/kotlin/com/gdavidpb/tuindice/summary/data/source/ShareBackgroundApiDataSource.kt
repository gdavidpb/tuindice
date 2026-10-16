package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundsResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ShareBackgroundApiDataSource(
	private val ktorClient: HttpClient
) : ShareBackgroundRemoteDataRepository {
	override suspend fun getShareBackgrounds(): List<ShareBackgroundResponse> {
		return ktorClient.get("users/v1/share-backgrounds")
			.body<ShareBackgroundsResponse>()
			.backgrounds
	}
}
