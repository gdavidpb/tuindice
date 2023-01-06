package com.gdavidpb.tuindice.di.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.utils.extensions.sharedPreferences
import com.gdavidpb.tuindice.data.source.AuthMockDataSource
import com.gdavidpb.tuindice.data.source.DebugKoinDataSource
import com.gdavidpb.tuindice.data.source.MessagingMockDataSource
import com.gdavidpb.tuindice.data.source.RemoteConfigMockDataSource
import com.gdavidpb.tuindice.data.source.android.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.crashlytics.DebugReportingDataSource
import com.gdavidpb.tuindice.data.source.functions.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.source.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@KoinReflectAPI
val appMockModule = module {

	/* Application */

	single {
		androidContext().sharedPreferences()
	}

	/* Android Services */

	single {
		androidContext().getSystemService<ConnectivityManager>()
	}

	single {
		androidContext().getSystemService<ActivityManager>()
	}

	single {
		androidContext().contentResolver
	}

	single {
		androidContext().resources
	}

	/* Utils */

	singleOf(::Gson)

	/* Google */

	single<AppUpdateManager> {
		FakeAppUpdateManager(androidContext())
	}

	single {
		GoogleApiAvailability.getInstance()
	}

	/* Firebase */

	single {
		FirebaseRemoteConfig.getInstance().apply {
			setDefaultsAsync(R.xml.default_remote_config)
		}
	}

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

	singleOf(::AuthorizationInterceptor)

	factory<ReviewManager> {
		FakeReviewManager(androidContext())
	}

	/* Repositories */

	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::AuthMockDataSource) { bind<AuthRepository>() }
	factoryOf(::MessagingMockDataSource) { bind<MessagingRepository>() }
	factoryOf(::RemoteConfigMockDataSource) { bind<ConfigRepository>() }
	factoryOf(::DebugReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::DebugKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }

	/* Utils */

	single {
		Picasso.get()
	}
}