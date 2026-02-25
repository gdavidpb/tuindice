package com.gdavidpb.tuindice.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserGateway
import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoGateway
import com.gdavidpb.tuindice.base.domain.repository.ExternalActions
import com.gdavidpb.tuindice.base.domain.repository.FileGateway
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.IntegrityGateway
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.ReviewGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SecureStore
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateGateway
import com.gdavidpb.tuindice.data.repository.attestation.AttestationDataRepository
import com.gdavidpb.tuindice.data.repository.attestation.PayloadDigestDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.ChallengeApiDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.PlayIntegrityDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.SHA256PayloadDigestDataSource
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.data.source.activity.InMemoryCurrentActivityProvider
import com.gdavidpb.tuindice.data.source.browser.AndroidBrowserGateway
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.source.FirebaseMessagingDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingApiDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingPreferencesDataSource
import com.gdavidpb.tuindice.data.source.actions.AndroidExternalActions
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.di.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.device.AndroidDeviceInfoGateway
import com.gdavidpb.tuindice.data.source.environment.BuildConfigEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashReporter
import com.gdavidpb.tuindice.data.source.reporting.FirebaseCrashReporter
import com.gdavidpb.tuindice.data.source.review.PlayReviewGateway
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.securestore.AndroidSecureStoreDataSource
import com.gdavidpb.tuindice.data.source.ui.AndroidHostUiTextProvider
import com.gdavidpb.tuindice.data.source.update.PlayUpdateGateway
import com.gdavidpb.tuindice.ui.screen.AndroidBrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.utils.UserAgent
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource as AttestationRemote
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource as MessagingLocal
import com.gdavidpb.tuindice.data.repository.messaging.ProviderDataSource as MessagingProvider
import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource as MessagingRemote

val appModule = module {
	/* Android Services */

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
		PreferenceDataStoreFactory.create(
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
			produceFile = {
				androidContext().preferencesDataStoreFile(DATASTORE_FILE_NAME)
			}
		)
	}

	single {
		AppUpdateManagerFactory.create(androidContext())
	}

	single {
		ReviewManagerFactory.create(androidContext())
	}

	/* Firebase */

	single {
		FirebaseRemoteConfig.getInstance().apply {
			setDefaultsAsync(R.xml.default_remote_config)
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

	/* KtorHttpClient */

		single {
			createSharedHttpClient(
				appEnvironmentRepository = get(),
				configRepository = get(),
				sessionRepository = get(),
				attestationRepositoryProvider = { get() },
				authApiRepositoryProvider = { get() },
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

	/* Repositories */

	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }
	factoryOf(::MessagingDataRepository) {
		bind<MessagingRepository>()
		bind<PushGateway>()
	}
	factoryOf(::AttestationDataRepository) {
		bind<AttestationRepository>()
		bind<IntegrityGateway>()
	}

	/* Data sources */

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::AndroidSecureStoreDataSource) {
		bind<SecureStoreDataSource>()
		bind<SecureStore>()
	}
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	singleOf(::PlayIntegrityDataSource) { bind<AttestationProvider>() }
	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::MessagingApiDataSource) { bind<MessagingRemote>() }
	singleOf(::ChallengeApiDataSource) { bind<AttestationRemote>() }
	singleOf(::SHA256PayloadDigestDataSource) { bind<PayloadDigestDataSource>() }
	singleOf(::FirebaseMessagingDataSource) { bind<MessagingProvider>() }
	singleOf(::MessagingPreferencesDataSource) { bind<MessagingLocal>() }
	singleOf(::AndroidApplicationDataSource) {
		bind<ApplicationRepository>()
		bind<FileGateway>()
	}
	singleOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	singleOf(::RemoteConfigDataSource) {
		bind<ConfigRepository>()
		bind<ConfigGateway>()
	}
	singleOf(::FirebaseCrashReporter) { bind<CrashReporter>() }
	singleOf(::CrashlyticsReportingDataSource) {
		bind<ReportingRepository>()
		bind<ReportingGateway>()
	}
	singleOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
	singleOf(::AndroidNetworkDataSource) {
		bind<NetworkRepository>()
		bind<NetworkStatusGateway>()
	}
	singleOf(::BuildConfigEnvironmentDataSource) {
		bind<AppEnvironmentRepository>()
		bind<AppEnvironmentGateway>()
	}
	singleOf(::InMemoryCurrentActivityProvider) { bind<CurrentActivityProvider>() }
	singleOf(::PlayReviewGateway) { bind<ReviewGateway>() }
	singleOf(::PlayUpdateGateway) { bind<UpdateGateway>() }
	singleOf(::AndroidBrowserGateway) { bind<BrowserGateway>() }
	singleOf(::AndroidBrowserScreenRenderer) { bind<BrowserScreenRenderer>() }
	singleOf(::AndroidHostUiTextProvider) { bind<HostUiTextProvider>() }
	singleOf(::AndroidExternalActions) { bind<ExternalActions>() }
	singleOf(::AndroidDeviceInfoGateway) { bind<DeviceInfoGateway>() }
}

private const val DATASTORE_FILE_NAME = "tuindice.preferences_pb"
