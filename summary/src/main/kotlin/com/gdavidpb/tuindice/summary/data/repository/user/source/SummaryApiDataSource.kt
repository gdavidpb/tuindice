package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.api.mapper.toProfilePicture
import com.gdavidpb.tuindice.summary.data.repository.user.source.api.mapper.toUser
import com.gdavidpb.tuindice.summary.data.repository.user.source.api.response.UserResponse
import com.gdavidpb.tuindice.summary.data.repository.user.source.api.response.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.toOutputStream
import java.io.InputStream

class SummaryApiDataSource(
	private val ktorClient: HttpClient
) : RemoteDataSource {
	override suspend fun getUser(): User {
		return ktorClient.get("users")
			.body<UserResponse>()
			.toUser()
	}

	override suspend fun uploadProfilePicture(inputStream: InputStream): ProfilePicture {
		return ktorClient.post("users/picture") {
			contentType(ContentType.Application.OctetStream)
			setBody(
				object : OutgoingContent.WriteChannelContent() {
					override suspend fun writeTo(channel: ByteWriteChannel) {
						val output = channel.toOutputStream()

						inputStream.use { input -> input.copyTo(output) }
					}
				}
			)
		}
			.body<ProfilePictureResponse>()
			.toProfilePicture()
	}

	override suspend fun removeProfilePicture() {
		ktorClient.delete("users/picture")
	}
}