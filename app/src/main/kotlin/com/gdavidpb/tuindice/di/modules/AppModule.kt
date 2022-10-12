package com.gdavidpb.tuindice.di.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.room.Room
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.*
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.crashlytics.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.dependencies.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.firebase.FirebaseAuthDataSource
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataSource
import com.gdavidpb.tuindice.data.source.functions.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.source.functions.CloudFunctionsDataSource
import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.data.source.room.utils.DatabaseModel
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataSource
import com.gdavidpb.tuindice.data.source.storage.FirebaseStorageDataSource
import com.gdavidpb.tuindice.data.source.storage.LocalStorageDataSource
import com.gdavidpb.tuindice.data.source.token.FirebaseCloudMessagingDataSource
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.base.utils.extensions.sharedPreferences
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import com.gdavidpb.tuindice.summary.domain.usecase.*
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
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
		androidContext().getSystemService<InputMethodManager>()
	}

	single {
		androidContext().getSystemService<ActivityManager>()
	}

	single {
		androidContext().packageManager
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

	single {
		AppUpdateManagerFactory.create(androidContext())
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
		FirebaseAuth.getInstance()
	}

	single {
		FirebaseFirestore.getInstance()
	}

	single {
		FirebaseStorage.getInstance()
	}

	single {
		FirebaseMessaging.getInstance()
	}

	single {
		FirebaseCrashlytics.getInstance()
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

	factory {
		ReviewManagerFactory.create(androidContext())
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
					.create<TuIndiceAPI>()
			}
	}

	/* Database */

	single {
		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, DatabaseModel.NAME)
			.fallbackToDestructiveMigration()
			.build()
	}

	/* View Models */

	viewModel<MainViewModel>()
	viewModel<SplashViewModel>()
	viewModel<SummaryViewModel>()
	viewModel<RecordViewModel>()
	viewModel<SignInViewModel>()
	viewModel<EvaluationPlanViewModel>()
	viewModel<EvaluationViewModel>()
	viewModel<PensumViewModel>()

	/* Repositories */

	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::LocalStorageDataSource) { bind<StorageRepository>() }
	factoryOf(::FirebaseStorageDataSource) { bind<RemoteStorageRepository>() }
	factoryOf(::FirebaseAuthDataSource) { bind<AuthRepository>() }
	factoryOf(::FirestoreDataSource) { bind<DatabaseRepository>() }
	factoryOf(::FirebaseCloudMessagingDataSource) { bind<MessagingRepository>() }
	factoryOf(::ContentResolverDataSource) { bind<ContentRepository>() }
	factoryOf(::RemoteConfigDataSource) { bind<ConfigRepository>() }
	factoryOf(::CrashlyticsReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
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