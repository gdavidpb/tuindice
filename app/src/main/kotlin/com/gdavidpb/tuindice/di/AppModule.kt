package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.extension.sharedPreferences
import com.gdavidpb.tuindice.data.android.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.crashlytics.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.fcm.FCMDataRepository
import com.gdavidpb.tuindice.data.fcm.FCMLocalDataSource
import com.gdavidpb.tuindice.data.fcm.FCMRemoteDataSource
import com.gdavidpb.tuindice.data.fcm.source.LocalDataSource
import com.gdavidpb.tuindice.data.fcm.source.RemoteDataSource
import com.gdavidpb.tuindice.data.firebase.FirebaseAuthDataSource
import com.gdavidpb.tuindice.data.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.koin.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.mutex.MutexDataSource
import com.gdavidpb.tuindice.data.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.retrofit.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.settings.PreferencesDataSource
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@KoinReflectAPI
val appModule = module {

	/* Application */

	single {
		androidContext().sharedPreferences()
	}

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

	/* Google */

	single {
		AppUpdateManagerFactory.create(androidContext())
	}

	single {
		GoogleApiAvailability.getInstance()
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
		FirebaseAuth.getInstance()
	}

	single {
		FirebaseMessaging.getInstance()
	}

	single {
		FirebaseCrashlytics.getInstance()
	}

	/* OkHttpClient */

	singleOf(::AuthorizationInterceptor)

	single {
		val logger = HttpLoggingInterceptor.Logger { message ->
			get<ReportingRepository>().logMessage(message)
		}

		HttpLoggingInterceptor(logger).apply {
			level = HttpLoggingInterceptor.Level.BODY

			redactHeader("Cookie")
			redactHeader("Authorization")
		}
	}

	single {
		val connectionTimeout = get<ConfigRepository>().getConnectionTimeout()

		OkHttpClient.Builder()
			.callTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.readTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.writeTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.addInterceptor(get<HttpLoggingInterceptor>())
			.addInterceptor(get<AuthorizationInterceptor>())
			.build()
	}

	/* Repositories */

	factoryOf(::FCMDataRepository) { bind<MessagingRepository>() }

	/* Data sources */

	factoryOf(::FCMRemoteDataSource) { bind<RemoteDataSource>() }
	factoryOf(::FCMLocalDataSource) { bind<LocalDataSource>() }
	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::FirebaseAuthDataSource) { bind<AuthRepository>() }
	factoryOf(::RemoteConfigDataSource) { bind<ConfigRepository>() }
	factoryOf(::CrashlyticsReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }

	/* Utils */

	singleOf(::MutexDataSource) { bind<ConcurrencyRepository>() }

	single {
		Picasso.get()
	}
}