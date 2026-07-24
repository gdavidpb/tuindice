package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
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
	private val externalActionsCapability: IosExternalActionsCapability
) : ApplicationRepository {
	override suspend fun canOpen(file: PlatformFile): Boolean {
		return externalActionsCapability.canOpen(file.path)
	}

	override suspend fun clearData() {
		// El orden importa: los settings guardan la marca de dueño de los datos locales
		// y deben limpiarse DESPUÉS de la base. Si `clearAll()` falla, la marca
		// sobrevive y el próximo inicio de sesión detecta que los datos son de otro.
		persistenceMaintenanceRepository.clearAll()
		attestationCapability.invalidateAttestationKeyId()
		settingsRepository.clear()
		runCatching { secureStore.clear() }
		runCatching { legacySecureStore.clear() }

		runCatching {
			FileSystem.SYSTEM.deleteRecursively(temporaryStorageRoot(), mustExist = false)
		}
	}
}
