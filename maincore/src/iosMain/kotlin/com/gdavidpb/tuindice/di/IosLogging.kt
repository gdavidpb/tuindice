package com.gdavidpb.tuindice.di

import platform.Foundation.NSLog

internal fun iosLog(message: String) {
	NSLog("%@", message)
}
