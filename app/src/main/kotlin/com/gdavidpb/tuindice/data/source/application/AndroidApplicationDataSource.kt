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
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.base.utils.extension.attempt
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CancellationException
import java.io.File

class AndroidApplicationDataSource(
	private val context: Context,
	private val persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
	private val settingsRepository: SettingsRepository,
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository,
	private val proofOfPossessionCapability: AndroidProofOfPossessionCapability,
	// Resolved when the wipe runs, not when this is built: some holders need this repository.
	private val sessionMemory: () -> List<SessionMemory>,
	private val sessionResidue: () -> List<SessionResidue>
) : ApplicationRepository {
	override suspend fun clearSessionResidue() {
		var firstFailure: Throwable? = null

		sessionResidue().forEach { residue ->
			runCatching { residue.clearSessionResidue() }
				.onFailure { failure ->
					if (failure is CancellationException) throw failure
					if (firstFailure == null) firstFailure = failure
				}
		}

		firstFailure?.let { failure -> throw failure }
	}

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
		// First, before anything is emptied: from here on, whatever a holder was waiting on (a
		// download that lands while the stores are being wiped) belongs to the account that is
		// leaving and must find that out. The same call in the finally below stays: it brings the
		// mirrors back to what the emptied stores say.
		releaseSessionMemory()

		try {
			persistenceMaintenanceRepository.clearAll()

			proofOfPossessionCapability.invalidateProofOfPossessionKeyId()
			settingsRepository.clear()
			attempt { secureStore.clear() }
			attempt { legacySecureStore.clear() }

			// The error reporter keeps what it has not sent yet in filesDir/.crashlytics.v3 (open sessions,
			// reports, priority and native reports; the reporter's FileStore roots it at
			// Context.getFilesDir). It is the app's and the device's, not the account's,
			// and it holds the very report of the invalidation that is running this wipe: it stays. The
			// older layouts it migrates away from are listed for the same reason. On iOS the reporter
			// writes to Library/Caches/com.crashlytics.data (FIRCLSFileManager), which the iOS wipe
			// (temporaryStorageRoot only) never touches.
			with(context) {
				runCatching {
					filesDir.listFiles().orEmpty()
						.filterNot { entry -> entry.name in REPORTER_FILE_NAMES }
						.forEach { entry -> runCatching { entry.deleteRecursively() } }
				}

				listOf(
					cacheDir,
					noBackupFilesDir,
					codeCacheDir
				).forEach { dir -> runCatching { dir.deleteRecursively() } }
			}
		} finally {
			// Last, and also when the wipe stops halfway: every holder goes back to what the
			// stores now say, so memory never keeps more than what is still stored.
			releaseSessionMemory()
		}
	}

	private suspend fun releaseSessionMemory() {
		sessionMemory().forEach { memory -> attempt { memory.clearSessionMemory() } }
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

// What the error reporter (Crashlytics) keeps in filesDir between runs: not data of the account.
internal val REPORTER_FILE_NAMES = setOf(
	".crashlytics.v3",
	".com.google.firebase.crashlytics",
	".com.google.firebase.crashlytics-ndk",
	".com.google.firebase.crashlytics.files.v1",
	".com.google.firebase.crashlytics.files.v2"
)
