package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSTemporaryDirectory

private const val DEFAULT_DATASTORE_FILE_NAME = "tuindice.preferences_pb"

internal fun createIosDataStore(
	fileName: String = DEFAULT_DATASTORE_FILE_NAME
): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(
		scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
		produceFile = {
			val path = persistentStorageRoot().resolve(fileName)
			FileSystem.SYSTEM.createDirectories(path.parent!!)
			path
		}
	)
}

internal fun temporaryStorageRoot(): Path {
	return "${NSTemporaryDirectory().trimEnd('/')}/tuindice".toPath()
}

internal fun persistentStorageRoot(): Path {
	return "${NSHomeDirectory().trimEnd('/')}/Library/Application Support/tuindice".toPath()
}
