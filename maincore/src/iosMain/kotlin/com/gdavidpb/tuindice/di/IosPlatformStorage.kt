package com.gdavidpb.tuindice.di

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

fun temporaryStorageRoot(): Path {
	return "${NSTemporaryDirectory().trimEnd('/')}/tuindice".toPath()
}