package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataRepository
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataRepository
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.data.source.IosAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.IosEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.IosShareTextHandler
import com.gdavidpb.tuindice.about.data.source.IosStoreUrlDataSource
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventSubscriber
import com.gdavidpb.tuindice.base.data.source.event.ReportingBreadcrumbEventSubscriber
import com.gdavidpb.tuindice.base.data.source.usage.NoOpUsageDataCollectionDataSource
import com.gdavidpb.tuindice.base.data.source.usage.UsageDataCollectionDataSource
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.secure.ACTIVE_SECURE_STORE_QUALIFIER
import com.gdavidpb.tuindice.base.data.source.secure.LEGACY_SECURE_STORE_QUALIFIER
import com.gdavidpb.tuindice.base.data.source.settings.APP_SECURE_STORE_NAME
import com.gdavidpb.tuindice.base.domain.coroutine.AppCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.data.source.analytics.IosAnalyticsEventSubscriber
import com.gdavidpb.tuindice.data.source.attestation.IosAttestationDataSource
import com.gdavidpb.tuindice.data.source.messaging.IosPushTokenDataSource
import com.gdavidpb.tuindice.data.source.actions.IosFileOpenerDataSource
import com.gdavidpb.tuindice.data.source.application.IosApplicationDataSource
import com.gdavidpb.tuindice.data.source.browser.IosBrowserDataSource
import com.gdavidpb.tuindice.data.source.config.IosRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.device.IosDeviceInfoDataSource
import com.gdavidpb.tuindice.data.source.environment.IosAppEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.IosNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.IosReportingDataSource
import com.gdavidpb.tuindice.data.source.review.IosReviewDataSource
import com.gdavidpb.tuindice.data.source.secure.IosSecureKeyValueDataSource
import com.gdavidpb.tuindice.data.source.update.IosUpdateDataSource
import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.data.source.KtorAuthApiDataSource
import com.gdavidpb.tuindice.persistence.di.registerIosPersistencePlatformStorage
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.platform.IosDeviceCapability
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.platform.IosObservabilityCapability
import com.gdavidpb.tuindice.platform.IOSContext
import com.gdavidpb.tuindice.platform.IosPushCapability
import com.gdavidpb.tuindice.platform.IosRemoteConfigCapability
import com.gdavidpb.tuindice.platform.IosReviewCapability
import com.gdavidpb.tuindice.platform.IosSecureStoreCapability
import com.gdavidpb.tuindice.platform.IosUpdateCapability
import com.gdavidpb.tuindice.platform.createIosUserAgent
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import com.gdavidpb.tuindice.summary.data.source.FileKitSkiaPictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.IosProfilePictureInputDataSource
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.IosBrowserScreenRenderer
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosPlatformModule = module {
	single<IOSContext> { iOSContext() }

	single<Settings.Factory> {
		NSUserDefaultsSettings.Factory()
	}
	single {
		KSafe(fileName = APP_SECURE_STORE_NAME)
	}
	single<SecureKeyValueDataRepository>(named(ACTIVE_SECURE_STORE_QUALIFIER)) {
		IosSecureKeyValueDataSource(capability = get())
	}
	registerIosPersistencePlatformStorage {
		iOSContext().databasePath
	}

	single<IosRemoteConfigCapability> { iOSContext().hostCapabilities.remoteConfig }
	single<IosAttestationCapability> { iOSContext().hostCapabilities.attestation }
	single<IosPushCapability> { iOSContext().hostCapabilities.push }
	single<IosReviewCapability> { iOSContext().hostCapabilities.review }
	single<IosUpdateCapability> { iOSContext().hostCapabilities.update }
	single<IosExternalActionsCapability> { iOSContext().hostCapabilities.externalActions }
	single<IosDeviceCapability> { iOSContext().hostCapabilities.device }
	single<IosObservabilityCapability> { iOSContext().hostCapabilities.observability }
	single<IosSecureStoreCapability> { iOSContext().hostCapabilities.secureStore }
	single<DefaultRemoteConfigValues> { iOSContext().configValues }

	single<EventSubscriber>(named("iosAnalyticsEventSubscriber")) {
		if (get<AppEnvironmentRepository>().getEnvironment().debug) {
			NoOpEventSubscriber
		} else {
			IosAnalyticsEventSubscriber(
				observabilityCapability = get()
			)
		}
	}
	single<EventSubscriber>(named("iosCrashlyticsBreadcrumbEventSubscriber")) {
		if (get<AppEnvironmentRepository>().getEnvironment().debug) {
			NoOpEventSubscriber
		} else {
			ReportingBreadcrumbEventSubscriber(
				reportingRepository = get()
			)
		}
	}
	single<AppStartupTask>(named("iosPerformanceCollectionTask")) {
		if (get<AppEnvironmentRepository>().getEnvironment().debug) {
			NoOpUsageDataCollectionDataSource
		} else {
			val observabilityCapability = get<IosObservabilityCapability>()
			UsageDataCollectionDataSource(
				usageDataConsentRepository = get(),
				setCollectionEnabledActions = listOf(
					observabilityCapability::setUsageDataCollectionEnabled,
					observabilityCapability::setPerformanceCollectionEnabled
				),
				coroutineScope = get<AppCoroutineScope>()
			)
		}
	}

	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::IosAppEnvironmentDataSource) { bind<AppEnvironmentRepository>() }
	singleOf(::IosRemoteConfigDataSource) { bind<RemoteConfigDataRepository>() }
	singleOf(::IosNetworkDataSource) { bind<NetworkRepository>() }
	singleOf(::IosDeviceInfoDataSource) { bind<DeviceInfoRepository>() }
	singleOf(::IosBrowserDataSource) { bind<BrowserRepository>() }
	singleOf(::IosBrowserScreenRenderer) { bind<BrowserScreenRenderer>() }
	singleOf(::IosFileOpenerDataSource) { bind<FileOpenerRepository>() }
	singleOf(::IosReviewDataSource) { bind<ReviewRepository>() }
	singleOf(::IosUpdateDataSource) { bind<UpdateRepository>() }
	single<IosApplicationDataSource> {
		IosApplicationDataSource(
			persistenceMaintenanceRepository = get(),
			settingsRepository = get(),
			secureStore = get(named(ACTIVE_SECURE_STORE_QUALIFIER)),
			legacySecureStore = get(named(LEGACY_SECURE_STORE_QUALIFIER)),
			attestationCapability = get(),
			externalActionsCapability = get()
		)
	}
	single<ApplicationRepository> { get<IosApplicationDataSource>() }
	single<FileRepository> { get<IosApplicationDataSource>() }
	singleOf(::IosReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::IosPushTokenDataSource) { bind<PushTokenDataRepository>() }
	factory<AuthApiDataRepository> {
		KtorAuthApiDataSource(
			ktorClient = get<HttpClient>(qualifier = named(IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}
	factory<AttestationRepository> {
		IosAttestationDataSource(
			httpClient = get<HttpClient>(qualifier = named(IDENTITY_HTTP_CLIENT_QUALIFIER)),
			attestationCapability = get<IosAttestationCapability>(),
			configRepository = get<ConfigRepository>(),
			sessionRepository = get(),
			recoverUnauthorizedSession = { attemptedAuthorizationAccessToken,
				attemptedCachedAccessToken,
				attemptedCachedRefreshToken ->
				get<com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository>().recoverUnauthorizedSession(
					attemptedAuthorizationAccessToken = attemptedAuthorizationAccessToken,
					attemptedCachedAccessToken = attemptedCachedAccessToken,
					attemptedCachedRefreshToken = attemptedCachedRefreshToken
				)
			}
		)
	}

	factoryOf(::IosEnvironmentDataSource) { bind<EnvironmentDataRepository>() }
	factoryOf(::IosAppInfoDataSource) { bind<AppInfoDataRepository>() }
	factory<StoreUrlRepository> {
		IosStoreUrlDataSource(appStoreUrl = iOSContext().appStoreUrl)
	}
	factoryOf(::IosShareTextHandler) { bind<ShareTextHandler>() }
	singleOf(::IosProfilePictureInputDataSource) { bind<ProfilePictureInputDataRepository>() }
	singleOf(::FileKitSkiaPictureEncoderDataSource) { bind<PictureEncoderDataRepository>() }

	single(named(IDENTITY_HTTP_CLIENT_QUALIFIER)) {
		createIdentityHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			settingsRepository = get<SettingsRepository>(),
			outdatedAppEventRepository = get(),
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
			settingsRepository = get<SettingsRepository>(),
			sessionRecoveryRepository = get(),
			outdatedAppEventRepository = get(),
			logger = createAppKtorLogger(),
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosDeviceCapability>())
		)
	}
}
