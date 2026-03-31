package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.contract.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.mapper.toProfilePicture
import com.gdavidpb.tuindice.summary.data.mapper.toUser
import com.gdavidpb.tuindice.summary.data.model.UserResponse
import com.gdavidpb.tuindice.summary.data.model.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class SummaryApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getUser(): User {
		return ktorClient.get("users/v1")
			.body<UserResponse>()
			.toUser()
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		return ktorClient.post("users/v1/picture") {
			contentType(ContentType.parse(mimeType))
			header(HttpHeaders.ContentLength, content.size.toString())
			setBody(content)
		}
			.body<ProfilePictureResponse>()
			.toProfilePicture()
	}

	override suspend fun removeProfilePicture() {
		ktorClient.delete("users/v1/picture")
	}
}
