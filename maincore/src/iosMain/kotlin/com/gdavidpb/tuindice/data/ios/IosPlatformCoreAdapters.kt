package com.gdavidpb.tuindice.data.ios

import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
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
import com.gdavidpb.tuindice.platform.ios.IosDeviceCapability
import com.gdavidpb.tuindice.platform.ios.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.ios.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.ios.IosPushCapability
import com.gdavidpb.tuindice.platform.ios.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.ios.IosReviewCapability
import com.gdavidpb.tuindice.platform.ios.IosUpdateCapability
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

internal class IosAppEnvironmentDataSource(
	private val environment: AppEnvironment
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = environment
}

internal class IosRemoteConfigDataSource(
	private val remoteConfigCapability: IosRemoteConfigCapability
) : RemoteConfigDataSource {
	override suspend fun fetch() {
		remoteConfigCapability.fetchRemoteConfig()
	}

	override fun getString(key: String): String? {
		return remoteConfigCapability.remoteConfigString(key)
	}
}

internal class IosNetworkDataSource(
	private val deviceCapability: IosDeviceCapability
) : NetworkRepository {
	override fun isAvailable(): Boolean = deviceCapability.isNetworkAvailable()
}

internal class IosDeviceInfoGateway(
	private val deviceCapability: IosDeviceCapability
) : DeviceInfoRepository {
	override fun appVersionName(): String = deviceCapability.appVersionName()

	override fun appVersionCode(): Long = deviceCapability.appVersionCode()

	override fun hasCamera(): Boolean = deviceCapability.hasCamera()
}

internal class IosBrowserGateway(
	private val externalActionsCapability: IosExternalActionsCapability
) : BrowserRepository {
	override fun open(url: String) {
		externalActionsCapability.openUrl(url)
	}
}

internal class IosFileOpener(
	private val externalActionsCapability: IosExternalActionsCapability
) : FileOpenerRepository {
	override fun openFile(file: PlatformFile): Boolean {
		return externalActionsCapability.openFile(file.path)
	}
}

internal class IosReviewGateway(
	private val reviewCapability: IosReviewCapability
) : ReviewRepository {
	override suspend fun launchReview() {
		reviewCapability.launchReview()
	}
}

internal class IosUpdateGateway(
	private val updateCapability: IosUpdateCapability
) : UpdateRepository {
	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		return updateCapability.checkForUpdate(stalenessDays)
	}

	override suspend fun launchUpdate(action: UpdateAction) {
		updateCapability.launchUpdate(action)
	}
}

internal class IosReportingDataSource(
	private val observabilityCapability: IosObservabilityCapability
) : ReportingRepository {
	override fun setIdentifier(identifier: String) {
		observabilityCapability.setUserIdentifier(identifier)
	}

	override fun logException(throwable: Throwable) {
		observabilityCapability.logException(throwable)
	}

	override fun logMessage(message: String) {
		observabilityCapability.logMessage(message)
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		observabilityCapability.setCustomKey(key, value.toString())
	}
}

internal class IosPushTokenDataSource(
	private val pushCapability: IosPushCapability
) : PushTokenDataSource {
	override suspend fun getToken(): String {
		return pushCapability.pushToken()
			?.takeIf { token -> token.isNotBlank() }
			?: throw IllegalStateException("Push token unavailable on iOS bridge.")
	}
}
