package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.*
import com.gdavidpb.tuindice.base.data.source.config.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.data.ios.*
import com.gdavidpb.tuindice.login.data.repository.LoginAuthApiDataSource
import com.gdavidpb.tuindice.login.data.repository.LoginDataRepository
import com.gdavidpb.tuindice.login.data.repository.LoginMessagingApiDataSource
import com.gdavidpb.tuindice.login.data.repository.LoginMessagingDataSource
import com.gdavidpb.tuindice.login.data.source.KtorLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.data.source.KtorLoginMessagingApiDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
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

data class IosPlatformConfig(
	val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy_policy.html",
		termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions.html",
		debug = false
	),
	val configValues: IosConfigValues = IosConfigValues(),
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
	singleOf(::IosFileOpener) { bind<FileOpenerRepository>() }
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
	factory<LoginMessagingDataSource> { IosLoginMessagingDataSource(get<IosPlatformBridge>()) }
	factory<LoginAuthApiDataSource> {
		KtorLoginAuthApiDataSource(
			ktorClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}
	factory<LoginMessagingApiDataSource> {
		KtorLoginMessagingApiDataSource(
			ktorClient = get<HttpClient>()
		)
	}
	factory<LoginRepository> {
		LoginDataRepository(
			authApiDataSource = get<LoginAuthApiDataSource>(),
			messagingApiDataSource = get<LoginMessagingApiDataSource>(),
			messagingDataSource = get<LoginMessagingDataSource>(),
			sessionRepository = get<SessionRepository>(),
			reportingRepository = get<ReportingRepository>()
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
			loginRepositoryProvider = { get<LoginRepository>() },
			logger = IOS_KTOR_LOGGER,
			json = get<Json>(),
			userAgentValue = createIosUserAgent(bridge)
		)
	}
}
