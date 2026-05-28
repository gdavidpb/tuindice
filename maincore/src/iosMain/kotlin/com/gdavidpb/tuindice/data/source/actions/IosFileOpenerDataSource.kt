package com.gdavidpb.tuindice.data.source.actions

import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

class IosFileOpenerDataSource(
	private val externalActionsCapability: IosExternalActionsCapability
) : FileOpenerRepository {
	override fun openFile(file: PlatformFile): Boolean {
		return externalActionsCapability.openFile(file.path)
	}
}
