package com.gdavidpb.tuindice.about.data.contract

interface AppInfoDataSource {
	fun appVersionName(): String
	fun appVersionCode(): Long
}
