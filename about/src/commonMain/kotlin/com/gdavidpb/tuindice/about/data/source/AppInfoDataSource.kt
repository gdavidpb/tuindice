package com.gdavidpb.tuindice.about.data.source

interface AppInfoDataSource {
	fun appVersionName(): String
	fun appVersionCode(): Long
}
