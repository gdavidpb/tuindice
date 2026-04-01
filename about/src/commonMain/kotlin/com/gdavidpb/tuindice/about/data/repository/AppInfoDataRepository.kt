package com.gdavidpb.tuindice.about.data.repository

interface AppInfoDataRepository {
	fun appVersionName(): String
	fun appVersionCode(): Long
}
