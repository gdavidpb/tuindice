package com.gdavidpb.tuindice.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidShareTextHandler
import com.gdavidpb.tuindice.about.data.source.AndroidStoreUrlDataSource
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.data.source.*
import com.gdavidpb.tuindice.base.data.source.config.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.base.utils.extension.toFirebaseDefaultsMap
import com.gdavidpb.tuindice.data.repository.attestation.AttestationDataRepository
import com.gdavidpb.tuindice.data.repository.attestation.PayloadDigestDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.ChallengeApiDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.PlayIntegrityDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.SHA256PayloadDigestDataSource
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.FirebasePushTokenDataSource
import com.gdavidpb.tuindice.data.source.actions.AndroidFileOpenerDataSource
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.data.source.activity.InMemoryCurrentActivityProvider
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.browser.AndroidBrowserDataSource
import com.gdavidpb.tuindice.data.source.config.AndroidRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.device.AndroidDeviceInfoDataSource
import com.gdavidpb.tuindice.data.source.environment.BuildConfigEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashReporter
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.reporting.FirebaseCrashReporter
import com.gdavidpb.tuindice.data.source.review.PlayReviewDataSource
import com.gdavidpb.tuindice.data.source.securestore.AndroidSecureStoreDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.update.PlayUpdateDataSource
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.ui.screen.AndroidBrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.utils.UserAgent
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository as BaseExternalActionsRepository
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource as AttestationRemote

val androidPlatformModule = module {
	registerAndroidPlatformStorage()
	registerAndroidPlatformPrimitives()
	registerAndroidPlatformServices()
	registerAndroidFeaturePlatformBindings()
	registerAndroidPlatformNetworking()
}

private fun Module.registerAndroidPlatformStorage() {
	single {
		androidContext().getSharedPreferences(
			androidContext().packageName,
			Context.MODE_PRIVATE
		)
	}

	single<DataStore<Preferences>> {
		val dataStoreFileName = "tuindice.preferences"

		PreferenceDataStoreFactory.create(
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
			produceFile = {
				androidContext().preferencesDataStoreFile(dataStoreFileName)
			}
		)
	}

	single {
		val name = androidContext().packageName

		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, name)
			.build()
	}
}

private fun Module.registerAndroidPlatformPrimitives() {
	single {
		androidContext().getSystemService<ConnectivityManager>()
	}

	single {
		androidContext().contentResolver
	}

	single {
		androidContext().resources
	}

	single {
		AppUpdateManagerFactory.create(androidContext())
	}

	single {
		ReviewManagerFactory.create(androidContext())
	}

	single<RemoteConfigDefaultsProfile> {
		if (BuildConfig.DEBUG) {
			RemoteConfigDefaultsProfile.DEBUG
		} else {
			RemoteConfigDefaultsProfile.PRODUCTION
		}
	}

	single {
		val defaultFetchInterval = 43_200L
		val remoteConfigProfile = get<RemoteConfigDefaultsProfile>()
		val fetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else defaultFetchInterval

		FirebaseRemoteConfig.getInstance().apply {
			val settings = FirebaseRemoteConfigSettings.Builder()
				.setMinimumFetchIntervalInSeconds(fetchIntervalSeconds)
				.build()
			setConfigSettingsAsync(settings)
			setDefaultsAsync(DefaultRemoteConfig.values(remoteConfigProfile).toFirebaseDefaultsMap())
		}
	}

	single {
		FirebaseMessaging.getInstance()
	}

	single {
		FirebaseCrashlytics.getInstance()
	}

	single {
		IntegrityManagerFactory.create(androidContext())
	}
}

private fun Module.registerAndroidPlatformServices() {
	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	singleOf(::AndroidRemoteConfigDataSource) { bind<RemoteConfigDataSource>() }
	singleOf(::FirebasePushTokenDataSource) { bind<PushTokenDataSource>() }
	singleOf(::InMemoryCurrentActivityProvider) { bind<CurrentActivityProvider>() }
	singleOf(::PlayReviewDataSource) { bind<ReviewRepository>() }
	singleOf(::PlayUpdateDataSource) { bind<UpdateRepository>() }
	singleOf(::AndroidBrowserDataSource) { bind<BrowserRepository>() }
	singleOf(::AndroidBrowserScreenRenderer) { bind<BrowserScreenRenderer>() }
	singleOf(::AndroidFileOpenerDataSource) { bind<BaseExternalActionsRepository>() }
	singleOf(::AndroidDeviceInfoDataSource) { bind<DeviceInfoRepository>() }
	singleOf(::AndroidSecureStoreDataSource) {
		bind<SecureStoreDataSource>()
		bind<SecureStoreRepository>()
	}
	singleOf(::AndroidApplicationDataSource) {
		bind<ApplicationRepository>()
		bind<FileRepository>()
	}
	singleOf(::FirebaseCrashReporter) { bind<CrashReporter>() }
	singleOf(::CrashlyticsReportingDataSource) {
		bind<ReportingRepository>()
	}
	singleOf(::AndroidNetworkDataSource) {
		bind<NetworkRepository>()
	}
	singleOf(::BuildConfigEnvironmentDataSource) {
		bind<AppEnvironmentRepository>()
	}
	single<ConfigRepository> {
		ConfigDataSource(
			remoteConfigDataSource = get<RemoteConfigDataSource>(),
			defaults = DefaultRemoteConfig.values(get<RemoteConfigDefaultsProfile>())
		)
	}
}

private fun Module.registerAndroidFeaturePlatformBindings() {
	factoryOf(::AndroidEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::AndroidAppInfoDataSource) { bind<AppInfoDataSource>() }
	factoryOf(::AndroidStoreUrlDataSource) { bind<StoreUrlDataSource>() }
	factoryOf(::AndroidShareTextHandler) { bind<ShareTextHandler>() }
}

private fun Module.registerAndroidPlatformNetworking() {
	singleOf(::PlayIntegrityDataSource) { bind<AttestationProvider>() }
	singleOf(::ChallengeApiDataSource) { bind<AttestationRemote>() }
	singleOf(::SHA256PayloadDigestDataSource) { bind<PayloadDigestDataSource>() }

	factoryOf(::AttestationDataRepository) {
		bind<AttestationRepository>()
	}

	single {
		createSharedHttpClient(
			appEnvironmentRepository = get(),
			configRepository = get(),
			sessionRepository = get(),
			attestationRepositoryProvider = { get() },
			loginRepositoryProvider = { get() },
			logger = createAppKtorLogger(),
			json = get(),
			userAgentValue = runCatching { UserAgent(androidContext()).toString() }.getOrNull()
		)
	}
}
