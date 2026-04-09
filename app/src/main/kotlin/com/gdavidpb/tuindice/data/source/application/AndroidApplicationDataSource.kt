package com.gdavidpb.tuindice.data.source.application

import android.content.Context
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import eu.anifantakis.lib.ksafe.KSafe
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.io.File

class AndroidApplicationDataSource(
	private val context: Context,
	private val persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
	private val settingsRepository: SettingsRepository,
	private val kSafe: KSafe,
	private val proofOfPossessionCapability: AndroidProofOfPossessionCapability
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
		persistenceMaintenanceRepository.clearAll()

		proofOfPossessionCapability.invalidateProofOfPossessionKeyId()
		settingsRepository.clear()
		kSafe.clearAll()

		with(context) {
			listOf(
				filesDir,
				cacheDir,
				noBackupFilesDir,
				codeCacheDir
			).forEach { dir -> runCatching { dir.deleteRecursively() } }
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
