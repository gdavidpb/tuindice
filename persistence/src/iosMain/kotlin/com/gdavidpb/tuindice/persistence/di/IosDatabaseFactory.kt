package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

fun createIosDatabase(
	path: String
): TuIndiceDatabase {
	require(path.isNotBlank()) {
		"iOS database path cannot be blank."
	}

	return Room.databaseBuilder<TuIndiceDatabase>(
		name = path
	)
		.fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5)
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.Default)
		.build()
}

@OptIn(ExperimentalForeignApi::class)
fun defaultIosDatabasePath(
	fileName: String = "tuindice.db"
): String {
	val documentsDirectory = NSFileManager.defaultManager
		.URLForDirectory(
			directory = NSDocumentDirectory,
			inDomain = NSUserDomainMask,
			appropriateForURL = null,
			create = true,
			error = null
		)
		?.path

	return if (documentsDirectory.isNullOrBlank()) {
		"${NSTemporaryDirectory().trimEnd('/')}/$fileName"
	} else {
		"$documentsDirectory/$fileName"
	}
}
