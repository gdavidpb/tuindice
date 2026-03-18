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
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.attestation.IosAttestationDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.source.IosPushTokenDataSource
import com.gdavidpb.tuindice.data.source.actions.IosFileOpenerDataSource
import com.gdavidpb.tuindice.data.source.application.IosApplicationDataSource
import com.gdavidpb.tuindice.data.source.browser.IosBrowserDataSource
import com.gdavidpb.tuindice.data.source.config.IosRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.device.IosDeviceInfoDataSource
import com.gdavidpb.tuindice.data.source.environment.IosAppEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.IosNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.IosReportingDataSource
import com.gdavidpb.tuindice.data.source.review.IosReviewDataSource
import com.gdavidpb.tuindice.data.source.update.IosUpdateDataSource
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataSource
import com.gdavidpb.tuindice.auth.data.source.KtorAuthApiDataSource
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import com.gdavidpb.tuindice.platform.IOS_IDENTITY_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.platform.IosDeviceCapability
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.IosPushCapability
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.IosReviewCapability
import com.gdavidpb.tuindice.platform.IosUpdateCapability
import com.gdavidpb.tuindice.platform.createIosIdentityHttpClient
import com.gdavidpb.tuindice.platform.createIosUserAgent
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
	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(iOSContext().appEnvironment) }
	singleOf(::IosRemoteConfigDataSource) { bind<RemoteConfigDataSource>() }
	singleOf(::IosNetworkDataSource) { bind<NetworkRepository>() }
	singleOf(::IosDeviceInfoDataSource) { bind<DeviceInfoRepository>() }
	singleOf(::IosBrowserDataSource) { bind<BrowserRepository>() }
	singleOf(::IosBrowserScreenRenderer) { bind<BrowserScreenRenderer>() }
	singleOf(::IosFileOpenerDataSource) { bind<FileOpenerRepository>() }
	singleOf(::IosReviewDataSource) { bind<ReviewRepository>() }
	singleOf(::IosUpdateDataSource) { bind<UpdateRepository>() }
	singleOf(::IosApplicationDataSource) {
		bind<ApplicationRepository>()
		bind<FileRepository>()
	}
	singleOf(::IosReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::IosPushTokenDataSource) { bind<PushTokenDataSource>() }
	factory<AuthApiDataSource> {
		KtorAuthApiDataSource(
			ktorClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}
	factory<AttestationRepository> {
		IosAttestationDataRepository(
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
			attestationRepositoryProvider = { get<AttestationRepository>() },
			authRepositoryProvider = { get<AuthRepository>() },
			credentialsRepositoryProvider = { get<CredentialsRepository>() },
			syncRepositoryProvider = { get<SyncRepository>() },
			logger = createAppKtorLogger(),
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosDeviceCapability>())
		)
	}
}
