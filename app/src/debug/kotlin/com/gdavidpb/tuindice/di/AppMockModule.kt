package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.work.WorkManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.sharedPreferences
import com.gdavidpb.tuindice.data.AuthMockDataSource
import com.gdavidpb.tuindice.data.DebugKoinDataSource
import com.gdavidpb.tuindice.data.MessagingMockDataSource
import com.gdavidpb.tuindice.data.RemoteConfigMockDataSource
import com.gdavidpb.tuindice.data.android.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.crashlytics.DebugReportingDataSource
import com.gdavidpb.tuindice.data.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.retrofit.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.settings.PreferencesDataSource
import com.gdavidpb.tuindice.evaluations.data.resolution.EvaluationResolutionHandler
import com.gdavidpb.tuindice.record.data.api.parser.QuarterRemoveParser
import com.gdavidpb.tuindice.record.data.api.parser.SubjectUpdateParser
import com.gdavidpb.tuindice.record.data.room.resolution.QuarterResolutionHandler
import com.gdavidpb.tuindice.record.data.room.resolution.SubjectResolutionHandler
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionInterceptor
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionParser
import com.gdavidpb.tuindice.transactions.data.room.resolution.ResolutionApplier
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
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
		androidContext().contentResolver
	}

	single {
		androidContext().resources
	}

	single {
		ResourceResolver(androidContext())
	}

	/* Google */

	single {
		WorkManager.getInstance(androidContext())
	}

	single<AppUpdateManager> {
		FakeAppUpdateManager(androidContext())
	}

	single {
		GoogleApiAvailability.getInstance()
	}

	single<ReviewManager> {
		FakeReviewManager(androidContext())
	}

	/* Firebase */

	single {
		FirebaseRemoteConfig.getInstance().apply {
			setDefaultsAsync(R.xml.default_remote_config)
		}
	}

	single {
		FirebaseAuth.getInstance().apply {
			useEmulator("10.0.2.2", 9099)
		}
	}

	/* OkHttpClient */

	singleOf(::AuthorizationInterceptor)
	singleOf(::TransactionInterceptor)

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
			.addInterceptor(get<TransactionInterceptor>())
			.build()
	}

	/* Utils */

	singleOf(::Gson)

	single {
		TransactionParser(
			parsers = listOf(
				get<SubjectUpdateParser>(),
				get<QuarterRemoveParser>()
			)
		)
	}

	single {
		ResolutionApplier(
			room = get(),
			resolutionHandlers = listOf(
				get<SubjectResolutionHandler>(),
				get<QuarterResolutionHandler>(),
				get<EvaluationResolutionHandler>()
			)
		)
	}

	/* Data sources */

	factoryOf(::MessagingMockDataSource) { bind<MessagingRepository>() }
	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::RemoteConfigMockDataSource) { bind<ConfigRepository>() }
	factoryOf(::AuthMockDataSource) { bind<AuthRepository>() }
	factoryOf(::DebugReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::DebugKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }
}