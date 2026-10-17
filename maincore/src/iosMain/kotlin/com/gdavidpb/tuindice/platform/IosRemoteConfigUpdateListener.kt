package com.gdavidpb.tuindice.platform

fun interface IosRemoteConfigUpdateListener {
	fun onUpdate(updatedKeys: Set<String>)
}
