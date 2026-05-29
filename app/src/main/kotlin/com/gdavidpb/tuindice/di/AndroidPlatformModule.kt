package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.about.data.repository.AppInfoDataRepository
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataRepository
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.data.source.AndroidAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidShareTextHandler
import com.gdavidpb.tuindice.about.data.source.AndroidStoreUrlDataSource
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.base.data.repository.*
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.data.source.settings.APP_SECURE_STORE_NAME
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.extension.toFirebaseDefaultsMap
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataRepository
import com.gdavidpb.tuindice.data.source.attestation.AndroidAttestationDataSource
import com.gdavidpb.tuindice.data.source.attestation.PlayIntegrityDataSource
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.data.source.messaging.FirebasePushTokenDataSource
import com.gdavidpb.tuindice.data.source.actions.AndroidFileOpenerDataSource
import com.gdavidpb.tuindice.data.source.analytics.FirebaseAnalyticsEventSubscriber
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.browser.AndroidBrowserDataSource
import com.gdavidpb.tuindice.data.source.config.AndroidRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.device.AndroidDeviceInfoDataSource
import com.gdavidpb.tuindice.data.source.environment.BuildConfigEnvironmentDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashReporterDataSource
import com.gdavidpb.tuindice.data.source.review.PlayReviewDataSource
import com.gdavidpb.tuindice.data.source.update.PlayUpdateDataSource
import com.gdavidpb.tuindice.persistence.di.registerAndroidPersistencePlatformStorage
import com.gdavidpb.tuindice.platform.android.AndroidKeystoreProofOfPossessionCapability
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.platform.android.UserAgent
import com.gdavidpb.tuindice.platform.android.androidDefaultConfigValues
import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import com.gdavidpb.tuindice.summary.data.source.AndroidProfilePictureInputDataSource
import com.gdavidpb.tuindice.ui.screen.AndroidBrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import eu.anifantakis.lib.ksafe.KSafe
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository as BaseExternalActionsRepository

val androidPlatformModule = module {
	registerAndroidPlatformStorage()
	registerAndroidPlatformPrimitives()
	registerAndroidPlatformServices()
	registerAndroidFeaturePlatformBindings()
	registerAndroidPlatformNetworking()
}

private fun Module.registerAndroidPlatformStorage() {
	single<Settings.Factory> {
		SharedPreferencesSettings.Factory(androidContext())
	}

	single {
		KSafe(
			context = androidContext(),
			fileName = APP_SECURE_STORE_NAME
		)
	}

	registerAndroidPersistencePlatformStorage()
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

	single<DefaultRemoteConfigValues> {
		androidDefaultConfigValues(isDebug = BuildConfig.DEBUG)
	}

	single {
		val defaultFetchInterval = 43_200L
		val fetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else defaultFetchInterval

		FirebaseRemoteConfig.getInstance().apply {
			val settings = FirebaseRemoteConfigSettings.Builder()
				.setMinimumFetchIntervalInSeconds(fetchIntervalSeconds)
				.build()
			setConfigSettingsAsync(settings)
			setDefaultsAsync(get<DefaultRemoteConfigValues>().toFirebaseDefaultsMap())
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

	single<StandardIntegrityManager> {
		IntegrityManagerFactory.createStandard(androidContext())
	}
}

private fun Module.registerAndroidPlatformServices() {
	if (!BuildConfig.DEBUG) {
		single { FirebaseAnalytics.getInstance(androidContext()) }
		single<EventSubscriber>(named("firebaseAnalyticsEventSubscriber")) {
			FirebaseAnalyticsEventSubscriber(
				firebaseAnalytics = get(),
				analyticsConsentRepository = get()
			)
		}
	}

	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::AndroidKeystoreProofOfPossessionCapability) { bind<AndroidProofOfPossessionCapability>() }
	singleOf(::AndroidRemoteConfigDataSource) { bind<RemoteConfigDataRepository>() }
	singleOf(::FirebasePushTokenDataSource) { bind<PushTokenDataRepository>() }
	singleOf(::CurrentActivityDataSource)
	singleOf(::PlayReviewDataSource) { bind<ReviewRepository>() }
	singleOf(::PlayUpdateDataSource) { bind<UpdateRepository>() }
	singleOf(::AndroidBrowserDataSource) { bind<BrowserRepository>() }
	singleOf(::AndroidBrowserScreenRenderer) { bind<BrowserScreenRenderer>() }
	singleOf(::AndroidFileOpenerDataSource) { bind<BaseExternalActionsRepository>() }
	singleOf(::AndroidDeviceInfoDataSource) { bind<DeviceInfoRepository>() }
	singleOf(::AndroidApplicationDataSource) {
		bind<ApplicationRepository>()
		bind<FileRepository>()
	}
	singleOf(::createCrashReporterDataSource)
	singleOf(::CrashlyticsReportingDataSource) {
		bind<ReportingRepository>()
	}
	singleOf(::AndroidNetworkDataSource) {
		bind<NetworkRepository>()
	}
	singleOf(::BuildConfigEnvironmentDataSource) {
		bind<AppEnvironmentRepository>()
	}
}

private fun Module.registerAndroidFeaturePlatformBindings() {
	factoryOf(::AndroidEnvironmentDataSource) { bind<EnvironmentDataRepository>() }
	factoryOf(::AndroidAppInfoDataSource) { bind<AppInfoDataRepository>() }
	factoryOf(::AndroidStoreUrlDataSource) { bind<StoreUrlRepository>() }
	factoryOf(::AndroidShareTextHandler) { bind<ShareTextHandler>() }
	singleOf(::AndroidProfilePictureInputDataSource) { bind<ProfilePictureInputDataRepository>() }
}

private fun Module.registerAndroidPlatformNetworking() {
	singleOf(::PlayIntegrityDataSource) { bind<AttestationProviderDataRepository>() }
	factoryOf(::AndroidAttestationDataSource) { bind<AttestationRepository>() }

	single {
		createSharedHttpClient(
			appEnvironmentRepository = get(),
			configRepository = get(),
			sessionRepository = get(),
			applicationRepository = get(),
			sessionInvalidationRepository = get(),
			syncStatusRepository = get(),
			attestationRepositoryProvider = { get() },
			authRepositoryProvider = { get() },
			credentialsRepositoryProvider = { get() },
			syncRepositoryProvider = { get() },
			logger = createAppKtorLogger(),
			json = get(),
			userAgentValue = runCatching { UserAgent(androidContext()).toString() }.getOrNull()
		)
	}
}

private fun createCrashReporterDataSource(
	crashlytics: FirebaseCrashlytics
): CrashReporterDataSource {
	return CrashReporterDataSource(
		setUserIdAction = crashlytics::setUserId,
		recordExceptionAction = crashlytics::recordException,
		logAction = crashlytics::log,
		setIntKeyAction = crashlytics::setCustomKey,
		setLongKeyAction = crashlytics::setCustomKey,
		setFloatKeyAction = crashlytics::setCustomKey,
		setDoubleKeyAction = crashlytics::setCustomKey,
		setStringKeyAction = crashlytics::setCustomKey,
		setBooleanKeyAction = crashlytics::setCustomKey
	)
}
