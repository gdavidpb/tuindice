package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import io.github.vinceglb.filekit.PlatformFile

class AndroidProfilePictureInputDataSource : ProfilePictureInputDataRepository {
	override suspend fun normalizeInput(file: PlatformFile): PlatformFile = file
}
