package com.gdavidpb.tuindice.data.source.android

import android.content.Context
import android.content.SharedPreferences
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.schema.DatabaseModel

class AndroidApplicationDataSource(
	private val sharedPreferences: SharedPreferences,
	private val room: TuIndiceDatabase,
	private val context: Context
) : ApplicationRepository {
	override suspend fun clearData() {
		room.clearAllTables()
		room.close()

		context.deleteDatabase(DatabaseModel.NAME)
		sharedPreferences.edit().clear().apply()

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