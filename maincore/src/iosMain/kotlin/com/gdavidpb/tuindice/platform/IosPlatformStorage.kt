package com.gdavidpb.tuindice.platform

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSTemporaryDirectory

fun temporaryStorageRoot(): Path {
	return "${NSTemporaryDirectory().trimEnd('/')}/tuindice".toPath()
}
