package com.gdavidpb.tuindice.about.data.repository

import com.gdavidpb.tuindice.about.domain.repository.AboutVersionTextProvider
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoGateway
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutDataRepositoryTest {
	@Test
	fun getVersionDescription_formatsVersionUsingEnvironmentAndDeviceInfo() = runBlocking {
		val textProvider = FakeAboutVersionTextProvider()
		val repository = AboutDataRepository(
			versionTextProvider = textProvider,
			deviceInfoGateway = FakeDeviceInfoGateway(
				versionName = "2.7.0",
				versionCode = 20700L
			),
			appEnvironmentRepository = FakeAppEnvironmentGateway(debug = true)
		)

		val versionDescription = repository.getVersionDescription()

		assertEquals("Debug 2.7.0 (20700)", versionDescription)
		assertEquals(true, textProvider.lastDebugValue)
		assertEquals("Debug", textProvider.lastEnvironmentName)
		assertEquals("2.7.0", textProvider.lastVersionName)
		assertEquals(20700L, textProvider.lastVersionCode)
	}
}

private class FakeAboutVersionTextProvider : AboutVersionTextProvider {
	var lastDebugValue: Boolean? = null
	var lastEnvironmentName: String? = null
	var lastVersionName: String? = null
	var lastVersionCode: Long? = null

	override fun environmentName(debug: Boolean): String {
		lastDebugValue = debug
		return if (debug) "Debug" else "Release"
	}

	override fun appVersion(
		environmentName: String,
		versionName: String,
		versionCode: Long
	): String {
		lastEnvironmentName = environmentName
		lastVersionName = versionName
		lastVersionCode = versionCode
		return "$environmentName $versionName ($versionCode)"
	}
}

private class FakeDeviceInfoGateway(
	private val versionName: String,
	private val versionCode: Long
) : DeviceInfoGateway {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode

	override fun hasCamera(): Boolean = true
}

private class FakeAppEnvironmentGateway(
	private val debug: Boolean
) : AppEnvironmentGateway {
	override fun getEnvironment(): AppEnvironment {
		return AppEnvironment(
			apiBaseUrl = "https://api.tuindice.app",
			privacyPolicyUrl = "https://tuindice.app/privacy",
			termsAndConditionsUrl = "https://tuindice.app/terms",
			debug = debug
		)
	}
}
