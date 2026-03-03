package com.gdavidpb.tuindice.di

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSTemporaryDirectory

internal fun temporaryStorageRoot(): Path {
	return "${NSTemporaryDirectory().trimEnd('/')}/tuindice".toPath()
}

internal fun persistentStorageRoot(): Path {
	return "${NSHomeDirectory().trimEnd('/')}/Library/Application Support/tuindice".toPath()
}
