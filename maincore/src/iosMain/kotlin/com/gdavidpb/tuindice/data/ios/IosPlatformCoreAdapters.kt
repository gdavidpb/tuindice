package com.gdavidpb.tuindice.data.ios

import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.di.IosPlatformBridge

internal class IosAppEnvironmentDataSource(
	private val environment: AppEnvironment
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = environment
}

internal class IosRemoteConfigDataSource(
	private val bridge: IosPlatformBridge
) : RemoteConfigDataSource {
	override suspend fun fetch() {
		bridge.fetchRemoteConfig()
	}

	override fun getString(key: String): String? {
		return bridge.remoteConfigString(key)
	}
}

internal class IosNetworkDataSource(
	private val bridge: IosPlatformBridge
) : NetworkRepository {
	override fun isAvailable(): Boolean = bridge.isNetworkAvailable()
}

internal class IosDeviceInfoGateway(
	private val bridge: IosPlatformBridge
) : DeviceInfoRepository {
	override fun appVersionName(): String = bridge.appVersionName()

	override fun appVersionCode(): Long = bridge.appVersionCode()

	override fun hasCamera(): Boolean = bridge.hasCamera()
}

internal class IosBrowserGateway(
	private val bridge: IosPlatformBridge
) : BrowserRepository {
	override fun open(url: String) {
		bridge.openUrl(url)
	}
}

internal class IosFileOpener(
	private val bridge: IosPlatformBridge
) : FileOpenerRepository {
	override fun openFile(fileRef: PlatformFileRef): Boolean {
		return bridge.openFile(fileRef)
	}
}

internal class IosReviewGateway(
	private val bridge: IosPlatformBridge
) : ReviewRepository {
	override suspend fun launchReview() {
		bridge.launchReview()
	}
}

internal class IosUpdateGateway(
	private val bridge: IosPlatformBridge
) : UpdateRepository {
	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		return bridge.checkForUpdate(stalenessDays)
	}

	override suspend fun launchUpdate(action: UpdateAction) {
		bridge.launchUpdate(action)
	}
}

internal class IosReportingDataSource(
	private val bridge: IosPlatformBridge
) : ReportingRepository {
	override fun setIdentifier(identifier: String) {
		bridge.setUserIdentifier(identifier)
	}

	override fun logException(throwable: Throwable) {
		bridge.logException(throwable)
	}

	override fun logMessage(message: String) {
		bridge.logMessage(message)
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		bridge.setCustomKey(key, value.toString())
	}
}

internal class IosPushTokenDataSource(
	private val bridge: IosPlatformBridge
) : PushTokenDataSource {
	override suspend fun getToken(): String {
		return bridge.pushToken()
			?.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable on iOS bridge.")
	}
}

internal class IosBridgeSecureStoreDataSource(
	private val bridge: IosPlatformBridge
) : SecureStoreDataSource {
	override fun contains(key: String): Boolean {
		return bridge.secureStoreContains(key)
	}

	override fun getString(key: String): String? {
		return bridge.secureStoreGetString(key)
	}

	override fun putString(key: String, value: String) {
		bridge.secureStorePutString(key, value)
	}

	override fun clear() {
		bridge.secureStoreClear()
	}
}
