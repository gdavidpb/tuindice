package com.gdavidpb.tuindice.data.source.application

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.utils.extension.canOpenFile
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.summary.utils.extension.fileProviderUri
import java.io.File

class AndroidApplicationDataSource(
	private val context: Context,
	private val room: TuIndiceDatabase,
	private val sharedPreferences: SharedPreferences
) : ApplicationRepository {
	override suspend fun createFile(path: String): String {
		val file = File(context.filesDir, path).apply {
			if (exists()) delete()
			createNewFile()
		}

		val providerUri = file.fileProviderUri(context)

		return "$providerUri"
	}

	override suspend fun canOpenFile(path: String): Boolean {
		return context.canOpenFile(file = File(path))
	}

	override suspend fun clearData() {
		room.clearAllTables()

		sharedPreferences.edit(commit = true) {
			clear()
		}

		with(context) {
			listOf(
				filesDir,
				cacheDir,
				noBackupFilesDir,
				codeCacheDir
			).forEach { dir -> runCatching { dir.deleteRecursively() } }
		}
	}
}