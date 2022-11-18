package com.gdavidpb.tuindice.di.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.*
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.data.source.crashlytics.DebugReportingDataSource
import com.gdavidpb.tuindice.data.source.functions.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.source.functions.CloudFunctionsDataSource
import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataSource
import com.gdavidpb.tuindice.data.source.storage.LocalStorageDataSource
import com.gdavidpb.tuindice.data.source.android.AndroidApplicationDataSource
import com.gdavidpb.tuindice.services.TuIndiceAPIMock
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.createMockService
import com.gdavidpb.tuindice.base.utils.extensions.sharedPreferences
import com.gdavidpb.tuindice.data.source.*
import com.gdavidpb.tuindice.evaluations.domain.usecase.*
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationPlanViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import com.gdavidpb.tuindice.record.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuarterUseCase
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.summary.domain.usecase.*
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@KoinReflectAPI
val mockModule = module {

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
		val settings = FirebaseFirestoreSettings.Builder()
			.setHost("${BuildConfig.URL_MOCK}:8080")
			.setSslEnabled(false)
			.setPersistenceEnabled(false)
			.build()

		FirebaseFirestore.getInstance().apply {
			firestoreSettings = settings
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

	/* TuIndice API */

	single {
		val connectionTimeout = get<ConfigRepository>().getLong(ConfigKeys.TIME_OUT_CONNECTION)

		OkHttpClient.Builder()
			.callTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.readTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.writeTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.addInterceptor(get<HttpLoggingInterceptor>())
			.addInterceptor(get<AuthorizationInterceptor>())
			.build().let { httpClient ->
				Retrofit.Builder()
					.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
					.addConverterFactory(GsonConverterFactory.create())
					.client(httpClient)
					.build()
					.createMockService<TuIndiceAPI, TuIndiceAPIMock>()
			}
	}

	/* View Models */

	viewModel<MainViewModel>()
	viewModel<SplashViewModel>()
	viewModel<SummaryViewModel>()
	viewModel<RecordViewModel>()
	viewModel<SignInViewModel>()
	viewModel<EvaluationPlanViewModel>()
	viewModel<EvaluationViewModel>()

	/* Repositories */

	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::LocalStorageDataSource) { bind<StorageRepository>() }
	factoryOf(::RemoteStorageMockDataSource) { bind<RemoteStorageRepository>() }
	factoryOf(::AuthMockDataSource) { bind<AuthRepository>() }
	factoryOf(::FirestoreMockDataSource) { bind<DatabaseRepository>() }
	factoryOf(::MessagingMockDataSource) { bind<MessagingRepository>() }
	factoryOf(::ContentResolverDataSource) { bind<ContentRepository>() }
	factoryOf(::RemoteConfigMockDataSource) { bind<ConfigRepository>() }
	factoryOf(::DebugReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::DebugKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }
	factoryOf(::CloudFunctionsDataSource) { bind<ServicesRepository>() }

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::ReSignInUseCase)
	factoryOf(::SignOutUseCase)
	factoryOf(::SyncUseCase)
	factoryOf(::StartUpUseCase)
	factoryOf(::GetProfileUseCase)
	factoryOf(::GetQuartersUseCase)
	factoryOf(::UpdateQuarterUseCase)
	factoryOf(::SetLastScreenUseCase)
	factoryOf(::GetEnrollmentProofUseCase)
	factoryOf(::GetSubjectUseCase)
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::GetSubjectEvaluationsUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
	factoryOf(::UpdateProfilePictureUseCase)
	factoryOf(::CreateProfilePictureFileUseCase)
	factoryOf(::GetProfilePictureFileUseCase)
	factoryOf(::GetProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Utils */

	single {
		Picasso.get()
	}
}