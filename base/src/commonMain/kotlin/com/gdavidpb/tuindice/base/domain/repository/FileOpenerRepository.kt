package com.gdavidpb.tuindice.base.domain.repository

import io.github.vinceglb.filekit.PlatformFile

interface FileOpenerRepository {
	fun openFile(file: PlatformFile): Boolean
}
