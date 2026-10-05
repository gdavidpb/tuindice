package com.gdavidpb.tuindice.data.source.application

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.io.File

class AndroidApplicationDataSource(
	private val context: Context,
	private val persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
	private val settingsRepository: SettingsRepository,
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository,
	private val proofOfPossessionCapability: AndroidProofOfPossessionCapability,
	// Resolved when the wipe runs, not when this is built: some holders need this repository.
	private val sessionMemory: () -> List<SessionMemory>
) : ApplicationRepository {
	override suspend fun canOpen(file: PlatformFile): Boolean {
		val source = file.path
		val uri = source.toUri()

		return if (uri.scheme == ContentResolver.SCHEME_CONTENT || uri.scheme == ContentResolver.SCHEME_FILE) {
			canOpenUri(uri = uri, mimeType = context.contentResolver.getType(uri))
		} else {
			val localFile = File(source)
			val providerUri = FileProvider.getUriForFile(context, context.packageName, localFile)
			val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(localFile.extension)

			canOpenUri(uri = providerUri, mimeType = mimeType)
		}
	}

	override suspend fun clearData() {
		try {
			persistenceMaintenanceRepository.clearAll()

			proofOfPossessionCapability.invalidateProofOfPossessionKeyId()
			settingsRepository.clear()
			runCatching { secureStore.clear() }
			runCatching { legacySecureStore.clear() }

			with(context) {
				listOf(
					filesDir,
					cacheDir,
					noBackupFilesDir,
					codeCacheDir
				).forEach { dir -> runCatching { dir.deleteRecursively() } }
			}
		} finally {
			// Last, and also when the wipe stops halfway: every holder goes back to what the
			// stores now say, so memory never keeps more than what is still stored.
			sessionMemory().forEach { memory -> runCatching { memory.clearSessionMemory() } }
		}
	}

	private fun canOpenUri(uri: Uri, mimeType: String?): Boolean {
		return runCatching {
			val intent = Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(uri, mimeType)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}

			intent.resolveActivity(context.packageManager) != null
		}.getOrDefault(false)
	}
}
