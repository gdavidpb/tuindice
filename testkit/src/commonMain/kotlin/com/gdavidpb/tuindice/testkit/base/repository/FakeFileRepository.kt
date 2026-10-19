package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import io.github.vinceglb.filekit.PlatformFile

class FakeFileRepository(
	private val canOpenResult: Boolean = true
) : FileRepository {
	var lastCanOpenFile: PlatformFile? = null

	override suspend fun canOpen(file: PlatformFile): Boolean {
		lastCanOpenFile = file
		return canOpenResult
	}
}
