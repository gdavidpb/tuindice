package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.data.contract.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.contract.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.contract.StoreUrlDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository

const val CURRENT_APP_VERSION_NAME = "5.8"
const val CURRENT_APP_VERSION_CODE = 36L
const val CURRENT_PRODUCTION_VERSION_TEXT = "Producción v$CURRENT_APP_VERSION_NAME ($CURRENT_APP_VERSION_CODE)"

class FakeAboutRepository(
	private val versionDescription: String = CURRENT_PRODUCTION_VERSION_TEXT
) : AboutRepository {
	override suspend fun getVersionDescription(): String = versionDescription
}

class FakeEnvironmentDataSource(
	private val isDebug: Boolean
) : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean = isDebug
}

class FakeAppInfoDataSource(
	private val versionName: String = CURRENT_APP_VERSION_NAME,
	private val versionCode: Long = CURRENT_APP_VERSION_CODE
) : AppInfoDataSource {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode
}

class FakeStoreUrlDataSource(
	private val storeUrl: String = "market://details?id=com.gdavidpb.tuindice"
) : StoreUrlDataSource {
	override fun getStoreUrl(): String = storeUrl
}
