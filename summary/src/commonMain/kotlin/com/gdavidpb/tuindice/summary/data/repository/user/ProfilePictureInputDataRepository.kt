package com.gdavidpb.tuindice.summary.data.repository.user

import io.github.vinceglb.filekit.PlatformFile

interface ProfilePictureInputDataRepository {
	suspend fun normalizeInput(file: PlatformFile): PlatformFile
}
