package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantAttachmentFileRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

// Reads the files the student picks with FileKit, the same way the photo encoders read them.
class PlatformAttachmentFileDataSource : AssistantAttachmentFileRepository {
	override suspend fun readBytes(file: PlatformFile): ByteArray = file.readBytes()
}
