package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.data.source.IosAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.IosEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.IosShareTextHandler
import com.gdavidpb.tuindice.about.data.source.IosStoreUrlDataSource
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.settings.APP_SECURE_STORE_NAME
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.data.ios.*
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.login.data.repository.LoginAuthApiDataSource
import com.gdavidpb.tuindice.login.data.source.KtorLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import com.gdavidpb.tuindice.platform.ios.IOS_IDENTITY_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.platform.ios.IosAttestationCapability
import com.gdavidpb.tuindice.platform.ios.IosDeviceCapability
import com.gdavidpb.tuindice.platform.ios.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.ios.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.ios.IosPushCapability
import com.gdavidpb.tuindice.platform.ios.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.ios.IosReviewCapability
import com.gdavidpb.tuindice.platform.ios.IosUpdateCapability
import com.gdavidpb.tuindice.platform.ios.createIosIdentityHttpClient
import com.gdavidpb.tuindice.platform.ios.createIosUserAgent
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.IosBrowserScreenRenderer
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosPlatformModule = module {
	registerIosPlatformStorage()
	registerIosPlatformPrimitives()
	registerIosPlatformServices()
	registerIosFeaturePlatformBindings()
	registerIosPlatformNetworking()
}

private fun Module.registerIosPlatformStorage() {
	single<Settings.Factory> {
		NSUserDefaultsSettings.Factory()
	}
	single {
		KSafe(fileName = APP_SECURE_STORE_NAME)
	}
	single<TuIndiceDatabase> {
		createIosDatabase(path = iOSContext().databasePath)
	}
}

private fun Module.registerIosPlatformPrimitives() {
	single<IosRemoteConfigCapability> { iOSContext().hostCapabilities.remoteConfig }
	single<IosAttestationCapability> { iOSContext().hostCapabilities.attestation }
	single<IosPushCapability> { iOSContext().hostCapabilities.push }
	single<IosReviewCapability> { iOSContext().hostCapabilities.review }
	single<IosUpdateCapability> { iOSContext().hostCapabilities.update }
	single<IosExternalActionsCapability> { iOSContext().hostCapabilities.externalActions }
	single<IosDeviceCapability> { iOSContext().hostCapabilities.device }
	single<IosObservabilityCapability> { iOSContext().hostCapabilities.observability }
	single<DefaultRemoteConfigValues> { iOSContext().configValues }
}

private fun Module.registerIosPlatformServices() {
	single<IdentifierRepository> { UUIDIdentifierDataSource() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(iOSContext().appEnvironment) }
	single<RemoteConfigDataSource> { IosRemoteConfigDataSource(get<IosRemoteConfigCapability>()) }
	single<NetworkRepository> { IosNetworkDataSource(get<IosDeviceCapability>()) }
	single<DeviceInfoRepository> { IosDeviceInfoGateway(get<IosDeviceCapability>()) }
	single<BrowserRepository> { IosBrowserGateway(get<IosExternalActionsCapability>()) }
	single<BrowserScreenRenderer> { IosBrowserScreenRenderer() }
	singleOf(::IosFileOpener) { bind<FileOpenerRepository>() }
	single<ReviewRepository> { IosReviewGateway(get<IosReviewCapability>()) }
	single<UpdateRepository> { IosUpdateGateway(get<IosUpdateCapability>()) }
	single<ApplicationRepository> {
		IosApplicationDataSource(
			settingsRepository = get<SettingsRepository>(),
			kSafe = get<KSafe>(),
			externalActionsCapability = get<IosExternalActionsCapability>()
		)
	}
	single<FileRepository> { get<ApplicationRepository>() }
	single<ReportingRepository> { IosReportingDataSource(get<IosObservabilityCapability>()) }
	factory<PushTokenDataSource> { IosPushTokenDataSource(get<IosPushCapability>()) }
	factory<LoginAuthApiDataSource> {
		KtorLoginAuthApiDataSource(
			ktorClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}
	factory<RiskAttestationRepository> {
		IosRiskAttestationDataRepository(
			httpClientProvider = {
				get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
			},
			attestationCapability = get<IosAttestationCapability>()
		)
	}
}

private fun Module.registerIosFeaturePlatformBindings() {
	factoryOf(::IosEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::IosAppInfoDataSource) { bind<AppInfoDataSource>() }
	factoryOf(::IosStoreUrlDataSource) { bind<StoreUrlDataSource>() }
	factoryOf(::IosShareTextHandler) { bind<ShareTextHandler>() }
}

private fun Module.registerIosPlatformNetworking() {
	single(named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)) {
		createIosIdentityHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			logger = createAppKtorLogger(),
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosDeviceCapability>())
		)
	}

	single {
		createSharedHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			sessionRepository = get<SessionRepository>(),
			riskAttestationRepositoryProvider = { get<RiskAttestationRepository>() },
			loginRepositoryProvider = { get<LoginRepository>() },
			logger = createAppKtorLogger(),
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosDeviceCapability>())
		)
	}
}
