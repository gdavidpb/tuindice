package com.gdavidpb.tuindice.assistant.domain.repository

import io.github.vinceglb.filekit.PlatformFile

/** Reads the bytes of a file the student picked, so it can be checked and sent. */
interface AssistantAttachmentFileRepository {
	suspend fun readBytes(file: PlatformFile): ByteArray
}
