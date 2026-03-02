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
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.ios.*
import com.gdavidpb.tuindice.login.data.repository.LoginAuthApiDataSource
import com.gdavidpb.tuindice.login.data.source.KtorLoginAuthApiDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
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

internal val iosPlatformModule: Module = module {
	registerIosPlatformStorage()
	registerIosPlatformPrimitives()
	registerIosPlatformServices()
	registerIosFeaturePlatformBindings()
	registerIosPlatformNetworking()
}

private fun Module.registerIosPlatformStorage() {
	single<SecureStoreDataSource> {
		iOSContext().secureStore ?: IosBridgeSecureStoreDataSource(get<IosSecureStoreCapability>())
	}
	single<SecureStoreRepository> { get<SecureStoreDataSource>() }
	single<DataStore<Preferences>> { iOSContext().dataStore }
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
	single<IosSecureStoreCapability> { iOSContext().hostCapabilities.secureStore }
}

private fun Module.registerIosPlatformServices() {
	single<IdentifierRepository> { UUIDIdentifierDataSource() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(iOSContext().appEnvironment) }
	single<RemoteConfigDataSource> { IosRemoteConfigDataSource(get<IosRemoteConfigCapability>()) }
	single<ConfigRepository> {
		ConfigDataSource(
			remoteConfigDataSource = get<RemoteConfigDataSource>(),
			defaults = iOSContext().configValues.toDefaultRemoteConfigValues()
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
