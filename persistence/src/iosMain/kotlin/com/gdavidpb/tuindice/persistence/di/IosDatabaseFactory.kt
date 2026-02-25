package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDomainMask
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSTemporaryDirectory

fun createIosDatabase(
	path: String
): TuIndiceDatabase {
	require(path.isNotBlank()) {
		"iOS database path cannot be blank."
	}

	return Room.databaseBuilder<TuIndiceDatabase>(
		name = path
	)
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.Default)
		.build()
}

fun createDefaultIosDatabase(
	fileName: String = "tuindice.db"
): TuIndiceDatabase {
	return createIosDatabase(
		path = defaultIosDatabasePath(fileName)
	)
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
