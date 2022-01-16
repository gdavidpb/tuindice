package com.gdavidpb.tuindice.di.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.crashlytics.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.dependencies.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.firebase.FirebaseDataSource
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataSource
import com.gdavidpb.tuindice.data.source.functions.AuthorizationInterceptor
import com.gdavidpb.tuindice.data.source.functions.CloudFunctionsDataSource
import com.gdavidpb.tuindice.data.source.functions.TuIndiceAPI
import com.gdavidpb.tuindice.data.source.google.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataSource
import com.gdavidpb.tuindice.data.source.storage.FirebaseStorageDataSource
import com.gdavidpb.tuindice.data.source.storage.LocalStorageDataSource
import com.gdavidpb.tuindice.data.source.token.FirebaseCloudMessagingDataSource
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.extensions.create
import com.gdavidpb.tuindice.utils.extensions.sharedPreferences
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
import org.koin.dsl.bind
import org.koin.dsl.factory
import org.koin.dsl.module
import org.koin.dsl.single
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

    single<Gson>()

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

    single<AuthorizationInterceptor>()

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

    factory<PreferencesDataSource>() bind SettingsRepository::class
    factory<LocalStorageDataSource>() bind StorageRepository::class
    factory<FirebaseStorageDataSource>() bind RemoteStorageRepository::class
    factory<FirebaseDataSource>() bind AuthRepository::class
    factory<FirestoreDataSource>() bind DatabaseRepository::class
    factory<FirebaseCloudMessagingDataSource>() bind MessagingRepository::class
    factory<ContentResolverDataSource>() bind ContentRepository::class
    factory<RemoteConfigDataSource>() bind ConfigRepository::class
    factory<CrashlyticsReportingDataSource>() bind ReportingRepository::class
    factory<ReleaseKoinDataSource>() bind DependenciesRepository::class
    factory<AndroidNetworkDataSource>() bind NetworkRepository::class
    factory<GooglePlayServicesDataSource>() bind ServicesRepository::class
    factory<CloudFunctionsDataSource>() bind ApiRepository::class

    /* Use cases */

    factory<SignInUseCase>()
    factory<ReSignInUseCase>()
    factory<SignOutUseCase>()
    factory<SyncUseCase>()
    factory<StartUpUseCase>()
    factory<GetProfileUseCase>()
    factory<GetQuartersUseCase>()
    factory<UpdateQuarterUseCase>()
    factory<SetLastScreenUseCase>()
    factory<GetEnrollmentProofUseCase>()
    factory<GetSubjectUseCase>()
    factory<GetEvaluationUseCase>()
    factory<GetSubjectEvaluationsUseCase>()
    factory<UpdateEvaluationUseCase>()
    factory<RemoveEvaluationUseCase>()
    factory<AddEvaluationUseCase>()
    factory<UpdateProfilePictureUseCase>()
    factory<CreateProfilePictureFileUseCase>()
    factory<GetProfilePictureFileUseCase>()
    factory<GetProfilePictureUseCase>()
    factory<RemoveProfilePictureUseCase>()
    factory<RequestReviewUseCase>()
    factory<RemoveQuarterUseCase>()
    factory<GetUpdateInfoUseCase>()

    /* Utils */

    single {
        Picasso.get()
    }
}