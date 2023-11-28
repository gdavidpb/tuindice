package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
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
import com.gdavidpb.tuindice.data.AttestationMockDataRepository
import com.gdavidpb.tuindice.data.AuthMockDataSource
import com.gdavidpb.tuindice.data.DebugKoinDataSource
import com.gdavidpb.tuindice.data.DebugReportingDataSource
import com.gdavidpb.tuindice.data.MessagingMockDataSource
import com.gdavidpb.tuindice.data.RemoteConfigMockDataSource
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.mobile.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.utils.retrofit.AttestationInterceptor
import com.gdavidpb.tuindice.data.utils.retrofit.AuthorizationInterceptor
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.resolution.EvaluationResolutionHandler
import com.gdavidpb.tuindice.presentation.action.browser.CloseBrowserDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.ConfirmOpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.CloseMainDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastScreenActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser.QuarterRemoveParser
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.parser.SubjectUpdateParser
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.QuarterResolutionHandler
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.SubjectResolutionHandler
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionInterceptor
import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionParser
import com.gdavidpb.tuindice.transactions.data.room.resolution.ResolutionApplier
import com.gdavidpb.tuindice.transactions.data.workmanager.SyncWorker
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val appMockModule = module {
	/* View Models */

	viewModelOf(::MainViewModel)
	viewModelOf(::BrowserViewModel)

	/* Action processor */

	factoryOf(::UpdateStateActionProcessor)
	factoryOf(::StartUpActionProcessor)
	factoryOf(::RequestReviewActionProcessor)
	factoryOf(::RequestUpdateActionProcessor)
	factoryOf(::SetLastScreenActionProcessor)
	factoryOf(::CloseMainDialogActionProcessor)

	factoryOf(::NavigateToActionProcessor)
	factoryOf(::SetLoadingActionProcessor)
	factoryOf(::OpenExternalResourceActionProcessor)
	factoryOf(::ConfirmOpenExternalResourceActionProcessor)
	factoryOf(::CloseBrowserDialogActionProcessor)

	/* Use cases */

	factoryOf(::StartUpUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::SetLastScreenUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Exception handlers */

	factoryOf(::StartUpExceptionHandler)

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

	single {
		androidContext().sharedPreferences()
	}

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

	workerOf(::SyncWorker) { bind<ListenableWorker>() }

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
	singleOf(::AttestationInterceptor)
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
			.addInterceptor(get<AttestationInterceptor>())
			.addInterceptor(get<TransactionInterceptor>())
			.build()
	}

	/* Utils */

	factory {
		Json.asConverterFactory("application/json".toMediaType())
	}

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

	/* Repositories */

	factoryOf(::MessagingMockDataSource) { bind<MessagingRepository>() }
	factoryOf(::AttestationMockDataRepository) { bind<AttestationRepository>() }

	/* Data sources */

	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::RemoteConfigMockDataSource) { bind<ConfigRepository>() }
	factoryOf(::AuthMockDataSource) { bind<AuthRepository>() }
	factoryOf(::DebugReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::DebugKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }
}