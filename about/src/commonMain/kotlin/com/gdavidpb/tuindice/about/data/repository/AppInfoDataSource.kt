package com.gdavidpb.tuindice.about.data.repository

interface AppInfoDataSource {
	fun appVersionName(): String
	fun appVersionCode(): Long
}
