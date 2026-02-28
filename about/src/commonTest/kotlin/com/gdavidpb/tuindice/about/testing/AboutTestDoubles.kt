package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository

class FakeAboutRepository(
	private val versionDescription: String = "Producción v3.5.1 (351)"
) : AboutRepository {
	override suspend fun getVersionDescription(): String = versionDescription
}

class FakeEnvironmentDataSource(
	private val isDebug: Boolean
) : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean = isDebug
}

class FakeAppInfoDataSource(
	private val versionName: String = "3.5.1",
	private val versionCode: Long = 351L
) : AppInfoDataSource {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode
}

class FakeStoreUrlDataSource(
	private val storeUrl: String = "market://details?id=com.gdavidpb.tuindice"
) : StoreUrlDataSource {
	override fun getStoreUrl(): String = storeUrl
}
