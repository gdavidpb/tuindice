package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import io.github.vinceglb.filekit.PlatformFile

class RecordingApplicationRepository(
	private val canOpenResult: Boolean = true,
	private val clearSessionResidueFailure: Throwable? = null
) : ApplicationRepository {
	var cleared = false
		private set
	var clearCalls = 0
		private set
	var clearSessionResidueCalls = 0
		private set
	var lastCanOpenFile: PlatformFile? = null

	override suspend fun clearSessionResidue() {
		clearSessionResidueCalls++
		clearSessionResidueFailure?.let { throw it }
	}

	override suspend fun clearData() {
		cleared = true
		clearCalls++
	}

	override suspend fun canOpen(file: PlatformFile): Boolean {
		lastCanOpenFile = file
		return canOpenResult
	}
}
