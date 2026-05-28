package com.gdavidpb.tuindice.base.domain.repository

import io.github.vinceglb.filekit.PlatformFile

interface FileRepository {
	suspend fun canOpen(file: PlatformFile): Boolean
}
