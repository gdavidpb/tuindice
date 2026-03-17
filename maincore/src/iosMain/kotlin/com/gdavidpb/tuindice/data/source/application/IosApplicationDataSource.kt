package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.temporaryStorageRoot
import eu.anifantakis.lib.ksafe.KSafe
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import okio.FileSystem

class IosApplicationDataSource(
	private val settingsRepository: SettingsRepository,
	private val kSafe: KSafe,
	private val externalActionsCapability: IosExternalActionsCapability
) : ApplicationRepository {
	override suspend fun canOpen(file: PlatformFile): Boolean {
		return externalActionsCapability.canOpen(file.path)
	}

	override suspend fun clearData() {
		settingsRepository.clear()
		kSafe.clearAll()

		runCatching {
			FileSystem.SYSTEM.deleteRecursively(temporaryStorageRoot(), mustExist = false)
		}
	}
}
