package com.gdavidpb.tuindice.platform

interface IosExternalActionsCapability {
	fun openUrl(url: String)
	fun openFile(path: String): Boolean
	fun canOpen(path: String): Boolean
}
