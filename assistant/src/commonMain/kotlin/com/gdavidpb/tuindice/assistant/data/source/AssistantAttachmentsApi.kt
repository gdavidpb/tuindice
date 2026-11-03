package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentHeaders
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentName
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

// An upload waits up to two minutes for the service to keep the photo: longer than the client's default for a text
// turn.
private const val ATTACHMENT_REQUEST_TIMEOUT_MILLIS = 120_000L

// The bytes go as the body, with their media type and the id the service checks them against, as the contract says.
internal suspend fun KtorAssistantApiDataSource.postAttachment(
	content: AssistantAttachmentContent,
	name: String
): AttachmentAccepted {
	return send {
		post(assistantPath("attachments")) {
			contentType(ContentType.parse(content.mediaType))
			header(HttpHeaders.ContentLength, content.bytes.size.toString())
			header(AttachmentHeaders.CLIENT_ATTACHMENT_ID, content.sha256)
			header(AttachmentHeaders.NAME, AttachmentName.encode(name))
			setBody(content.bytes)
			timeout { requestTimeoutMillis = ATTACHMENT_REQUEST_TIMEOUT_MILLIS }
		}
	}.decodeBody()
}
