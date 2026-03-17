package com.gdavidpb.tuindice.platform

interface IosPushCapability {
	suspend fun pushToken(): String?
}
