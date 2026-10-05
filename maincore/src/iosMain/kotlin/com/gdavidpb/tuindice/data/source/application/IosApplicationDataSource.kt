package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.temporaryStorageRoot
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import okio.FileSystem

class IosApplicationDataSource(
	private val persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
	private val settingsRepository: SettingsRepository,
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository,
	private val attestationCapability: IosAttestationCapability,
	private val externalActionsCapability: IosExternalActionsCapability,
	// Resolved when the wipe runs, not when this is built: some holders need this repository.
	private val sessionMemory: () -> List<SessionMemory>
) : ApplicationRepository {
	override suspend fun canOpen(file: PlatformFile): Boolean {
		return externalActionsCapability.canOpen(file.path)
	}

	override suspend fun clearData() {
		try {
			persistenceMaintenanceRepository.clearAll()
			attestationCapability.invalidateAttestationKeyId()
			settingsRepository.clear()
			runCatching { secureStore.clear() }
			runCatching { legacySecureStore.clear() }

			runCatching {
				FileSystem.SYSTEM.deleteRecursively(temporaryStorageRoot(), mustExist = false)
			}
		} finally {
			// Last, and also when the wipe stops halfway: every holder goes back to what the
			// stores now say, so memory never keeps more than what is still stored.
			sessionMemory().forEach { memory -> runCatching { memory.clearSessionMemory() } }
		}
	}
}
