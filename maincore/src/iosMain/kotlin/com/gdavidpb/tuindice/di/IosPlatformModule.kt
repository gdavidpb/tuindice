package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.data.source.IosAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.IosEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.IosShareTextHandler
import com.gdavidpb.tuindice.about.data.source.IosStoreUrlDataSource
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.data.source.*
import com.gdavidpb.tuindice.base.data.source.config.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.ios.*
import com.gdavidpb.tuindice.login.data.repository.LoginAuthApiDataSource
import com.gdavidpb.tuindice.login.data.source.KtorLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.IosImageEncoderDataSource
import com.gdavidpb.tuindice.summary.presentation.route.IosProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.ui.view.IosProfilePictureViewRenderer
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.IosBrowserScreenRenderer
import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal data class IosPlatformConfig(
	val hostCapabilities: IosHostCapabilities,
	val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy_policy.html",
		termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions.html",
		debug = false
	),
	val configValues: IosConfigValues = IosConfigValues(),
	val secureStore: SecureStoreDataSource? = null,
	val dataStore: DataStore<Preferences> = createIosDataStore(),
	val databasePath: String = defaultIosDatabasePath()
)

internal fun iosPlatformModule(
	config: IosPlatformConfig
): Module = module {
	registerIosPlatformStorage(config)
	registerIosHostCapabilities(config)
	registerIosPlatformServices(config)
	registerIosFeaturePlatformBindings()
	registerIosPlatformNetworking()
}

private fun Module.registerIosPlatformStorage(config: IosPlatformConfig) {
	single<SecureStoreDataSource> {
		config.secureStore ?: IosBridgeSecureStoreDataSource(get<IosSecureStoreCapability>())
	}
	single<SecureStoreRepository> { get<SecureStoreDataSource>() }
	single<DataStore<Preferences>> { config.dataStore }
	single<TuIndiceDatabase> {
		createIosDatabase(path = config.databasePath)
	}
}

private fun Module.registerIosHostCapabilities(config: IosPlatformConfig) {
	single<IosRemoteConfigCapability> { config.hostCapabilities.remoteConfig }
	single<IosAttestationCapability> { config.hostCapabilities.attestation }
	single<IosPushCapability> { config.hostCapabilities.push }
	single<IosReviewCapability> { config.hostCapabilities.review }
	single<IosUpdateCapability> { config.hostCapabilities.update }
	single<IosExternalActionsCapability> { config.hostCapabilities.externalActions }
	single<IosDeviceCapability> { config.hostCapabilities.device }
	single<IosObservabilityCapability> { config.hostCapabilities.observability }
	single<IosSecureStoreCapability> { config.hostCapabilities.secureStore }
}

private fun Module.registerIosPlatformServices(config: IosPlatformConfig) {
	single<IdentifierRepository> { UUIDIdentifierDataSource() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(config.appEnvironment) }
	single<RemoteConfigDataSource> { IosRemoteConfigDataSource(get<IosRemoteConfigCapability>()) }
	single<ConfigRepository> {
		ConfigDataSource(
			remoteConfigDataSource = get<RemoteConfigDataSource>(),
			defaults = config.configValues.toDefaultRemoteConfigValues()
		)
	}
	single<NetworkRepository> { IosNetworkDataSource(get<IosDeviceCapability>()) }
	single<DeviceInfoRepository> { IosDeviceInfoGateway(get<IosDeviceCapability>()) }
	single<BrowserRepository> { IosBrowserGateway(get<IosExternalActionsCapability>()) }
	single<BrowserScreenRenderer> { IosBrowserScreenRenderer() }
	singleOf(::IosFileOpener) { bind<FileOpenerRepository>() }
	single<ReviewRepository> { IosReviewGateway(get<IosReviewCapability>()) }
	single<UpdateRepository> { IosUpdateGateway(get<IosUpdateCapability>()) }
	single<SettingsRepository> { IosSettingsDataSource(get<DataStore<Preferences>>()) }
	single<ApplicationRepository> {
		IosApplicationDataSource(
			dataStore = get<DataStore<Preferences>>(),
			secureStoreDataSource = get<SecureStoreDataSource>(),
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
	factory<AttestationRepository> {
		IosAttestationDataRepository(
			httpClientProvider = {
				get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
			},
			json = get<Json>(),
			attestationCapability = get<IosAttestationCapability>()
		)
	}
}

private fun Module.registerIosFeaturePlatformBindings() {
	factoryOf(::IosEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::IosAppInfoDataSource) { bind<AppInfoDataSource>() }
	factoryOf(::IosStoreUrlDataSource) { bind<StoreUrlDataSource>() }
	factoryOf(::IosShareTextHandler) { bind<ShareTextHandler>() }
	factoryOf(::IosImageEncoderDataSource) { bind<PictureEncoderDataSource>() }
	factoryOf(::IosProfilePictureActionsFactory) { bind<ProfilePictureActionsFactory>() }
	factoryOf(::IosProfilePictureViewRenderer) { bind<ProfilePictureViewRenderer>() }
}

private fun Module.registerIosPlatformNetworking() {
	single(named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)) {
		createIosIdentityHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			logger = IOS_KTOR_LOGGER,
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
			loginRepositoryProvider = { get<LoginRepository>() },
			logger = IOS_KTOR_LOGGER,
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosDeviceCapability>())
		)
	}
}
