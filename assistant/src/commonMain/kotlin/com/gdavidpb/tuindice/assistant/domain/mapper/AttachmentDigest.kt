package com.gdavidpb.tuindice.assistant.domain.mapper

import io.ktor.util.sha256

// The id of an upload: the SHA-256 of its bytes in lowercase hex. The service checks it against the body, and keeps the
// first attachment of the same bytes for the student, under any name.
internal fun ByteArray.toAttachmentSha256(): String = sha256(this).toHexString()
