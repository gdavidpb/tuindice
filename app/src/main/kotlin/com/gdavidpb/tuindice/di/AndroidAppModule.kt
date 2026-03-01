package com.gdavidpb.tuindice.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
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
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.source.FirebaseMessagingDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingApiDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingPreferencesDataSource
import com.gdavidpb.tuindice.data.source.actions.AndroidFileOpenerDataSource
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.data.source.activity.InMemoryCurrentActivityProvider
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.browser.AndroidBrowserDataSource
import com.gdavidpb.tuindice.data.source.config.AndroidRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.device.AndroidDeviceInfoDataSource
import com.gdavidpb.tuindice.data.source.di.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.environment.BuildConfigEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashReporter
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.reporting.FirebaseCrashReporter
import com.gdavidpb.tuindice.data.source.review.PlayReviewDataSource
import com.gdavidpb.tuindice.data.source.securestore.AndroidSecureStoreDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.update.PlayUpdateDataSource
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
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository as BaseExternalActionsRepository
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource as AttestationRemote
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource as MessagingLocal
import com.gdavidpb.tuindice.data.repository.messaging.ProviderDataSource as MessagingProvider
import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource as MessagingRemote

val androidAppModule = module {
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

	single {
		createSharedHttpClient(
			appEnvironmentRepository = get(),
			configRepository = get(),
			sessionRepository = get(),
			attestationRepositoryProvider = { get() },
			loginRepositoryProvider = { get() },
			logger = get(),
			json = get(),
			userAgentValue = runCatching { UserAgent(androidContext()).toString() }.getOrNull()
		)
	}

	single<Logger> {
		object : Logger {
			override fun log(message: String) {
				get<ReportingRepository>().logMessage(message)
			}
		}
	}

	single {
		createSharedJson()
	}

	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }
	factoryOf(::MessagingDataRepository) {
		bind<MessagingRepository>()
	}
	factoryOf(::AttestationDataRepository) {
		bind<AttestationRepository>()
	}

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	singleOf(::PlayIntegrityDataSource) { bind<AttestationProvider>() }
	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::MessagingApiDataSource) { bind<MessagingRemote>() }
	singleOf(::ChallengeApiDataSource) { bind<AttestationRemote>() }
	singleOf(::SHA256PayloadDigestDataSource) { bind<PayloadDigestDataSource>() }
	singleOf(::FirebaseMessagingDataSource) { bind<MessagingProvider>() }
	singleOf(::MessagingPreferencesDataSource) { bind<MessagingLocal>() }
	singleOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	singleOf(::AndroidRemoteConfigDataSource) { bind<RemoteConfigDataSource>() }
	singleOf(::FirebaseCrashReporter) { bind<CrashReporter>() }
	singleOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
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
