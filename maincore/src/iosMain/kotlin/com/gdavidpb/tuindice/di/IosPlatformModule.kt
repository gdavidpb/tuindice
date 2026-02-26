package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.data.source.config.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SecureStoreRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.data.ios.IosAppEnvironmentDataSource
import com.gdavidpb.tuindice.data.ios.IosApplicationDataSource
import com.gdavidpb.tuindice.data.ios.IosAttestationDataRepository
import com.gdavidpb.tuindice.data.ios.IosBridgeSecureStoreDataSource
import com.gdavidpb.tuindice.data.ios.IosBrowserGateway
import com.gdavidpb.tuindice.data.ios.IosDependenciesDataSource
import com.gdavidpb.tuindice.data.ios.IosDeviceInfoGateway
import com.gdavidpb.tuindice.data.ios.IosExternalActions
import com.gdavidpb.tuindice.data.ios.IosLoginMessagingDataSource
import com.gdavidpb.tuindice.data.ios.IosLoginReportingDataSource
import com.gdavidpb.tuindice.data.ios.IosMessagingDataRepository
import com.gdavidpb.tuindice.data.ios.IosNetworkDataSource
import com.gdavidpb.tuindice.data.ios.IosRemoteConfigDataSource
import com.gdavidpb.tuindice.data.ios.IosReportingDataSource
import com.gdavidpb.tuindice.data.ios.IosReviewGateway
import com.gdavidpb.tuindice.data.ios.IosSettingsDataSource
import com.gdavidpb.tuindice.data.ios.IosUpdateGateway
import com.gdavidpb.tuindice.data.ios.StaticHostUiTextProvider
import com.gdavidpb.tuindice.login.data.repository.KtorAuthApiApiDataRepository
import com.gdavidpb.tuindice.login.data.repository.KtorMessagingApiDataRepository
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository as LoginMessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository as LoginMessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.IosBrowserScreenRenderer
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

data class IosPlatformConfig(
	val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy_policy.html",
		termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions.html",
		debug = false
	),
	val configValues: IosConfigValues = IosConfigValues(),
	val uiTextValues: IosUiTextValues = IosUiTextValues(),
	val bridge: IosPlatformBridge = DefaultIosPlatformBridge,
	val secureStore: SecureStoreDataSource? = null,
	val dataStore: DataStore<Preferences> = createIosDataStore()
)

fun iosPlatformModule(
	config: IosPlatformConfig = IosPlatformConfig()
): Module = module {
	/* Session */

	single<SecureStoreDataSource> {
		config.secureStore ?: IosBridgeSecureStoreDataSource(get<IosPlatformBridge>())
	}
	single<SecureStoreRepository> { get<SecureStoreDataSource>() }
	single<DataStore<Preferences>> { config.dataStore }
	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }

	/* Platform services */

	single<IosPlatformBridge> { config.bridge }
	single<HostUiTextProvider> { StaticHostUiTextProvider(config.uiTextValues) }
	single<IdentifierRepository> { UUIDIdentifierDataSource() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(config.appEnvironment) }
	single<RemoteConfigDataSource> { IosRemoteConfigDataSource(get<IosPlatformBridge>()) }
	single<ConfigRepository> {
		ConfigDataSource(
			remoteConfigDataSource = get<RemoteConfigDataSource>(),
			defaults = config.configValues.toDefaultRemoteConfigValues()
		)
	}
	single<NetworkRepository> { IosNetworkDataSource(get<IosPlatformBridge>()) }
	single<DependenciesRepository> { IosDependenciesDataSource(get<IosPlatformBridge>()) }
	single<DeviceInfoRepository> { IosDeviceInfoGateway(get<IosPlatformBridge>()) }
	single<BrowserRepository> { IosBrowserGateway(get<IosPlatformBridge>()) }
	single<BrowserScreenRenderer> { IosBrowserScreenRenderer() }
	single<ExternalActionsRepository> { IosExternalActions(get<IosPlatformBridge>()) }
	single<ReviewRepository> { IosReviewGateway(get<IosPlatformBridge>()) }
	single<UpdateRepository> { IosUpdateGateway(get<IosPlatformBridge>()) }
	single<SettingsRepository> { IosSettingsDataSource(get<DataStore<Preferences>>()) }
	single<ApplicationRepository> {
		IosApplicationDataSource(
			dataStore = get<DataStore<Preferences>>(),
			secureStoreDataSource = get<SecureStoreDataSource>(),
			bridge = get<IosPlatformBridge>()
		)
	}
	single<FileRepository> { get<ApplicationRepository>() }
	single<ReportingRepository> { IosReportingDataSource(get<IosPlatformBridge>()) }
	factory<LoginReportingRepository> { IosLoginReportingDataSource(get<IosPlatformBridge>()) }
	factory<LoginMessagingRepository> { IosLoginMessagingDataSource(get<IosPlatformBridge>()) }
	factory<AuthApiRepository> {
		KtorAuthApiApiDataRepository(
			ktorClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}
	factory<LoginMessagingApiRepository> {
		KtorMessagingApiDataRepository(
			ktorClient = get<HttpClient>()
		)
	}
	factory<MessagingRepository> {
		IosMessagingDataRepository(
			dataStore = get<DataStore<Preferences>>(),
			httpClientProvider = { get<HttpClient>() },
			bridge = get<IosPlatformBridge>()
		)
	}
	factory<AttestationRepository> {
		IosAttestationDataRepository(
			httpClientProvider = {
				get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
			},
			json = get<Json>(),
			bridge = get<IosPlatformBridge>()
		)
	}

	/* Serialization + network */

	single {
		createSharedJson()
	}

	single(named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)) {
		createIosIdentityHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			logger = IOS_KTOR_LOGGER,
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosPlatformBridge>())
		)
	}

	single {
		val bridge = get<IosPlatformBridge>()

		createSharedHttpClient(
			appEnvironmentRepository = get<AppEnvironmentRepository>(),
			configRepository = get<ConfigRepository>(),
			sessionRepository = get<SessionRepository>(),
			attestationRepositoryProvider = { get<AttestationRepository>() },
			authApiRepositoryProvider = { get<AuthApiRepository>() },
			logger = IOS_KTOR_LOGGER,
			json = get<Json>(),
			userAgentValue = createIosUserAgent(bridge)
		)
	}
}
