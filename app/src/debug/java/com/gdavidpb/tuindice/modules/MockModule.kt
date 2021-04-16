package com.gdavidpb.tuindice.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.crashlytics.DebugReportingDataSource
import com.gdavidpb.tuindice.data.source.dynamic.DynamicLinkDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.service.*
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataSource
import com.gdavidpb.tuindice.data.source.storage.LocalStorageDataSource
import com.gdavidpb.tuindice.datasources.*
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.services.DstAuthServiceMock
import com.gdavidpb.tuindice.services.DstEnrollmentServiceMock
import com.gdavidpb.tuindice.services.DstRecordServiceMock
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.createMockService
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module
import org.koin.experimental.builder.factory
import org.koin.experimental.builder.factoryBy
import org.koin.experimental.builder.single
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

val mockModule = module {

    /* Application */

    single {
        androidContext().encryptedSharedPreferences()
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
        FirebaseDynamicLinks.getInstance()
    }

    single {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    single<DstHostNameVerifier>()

    single<DstCookieJar>()

    single<DstAuthInterceptor>()

    factory {
        val connectionTimeout = get<ConfigRepository>().getLong(ConfigKeys.TIME_OUT_CONNECTION)

        OkHttpClient.Builder()
                .callTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .hostnameVerifier(get<DstHostNameVerifier>())
                .cookieJar(get<DstCookieJar>())
                .addInterceptor(get<HttpLoggingInterceptor>())
    }

    factory<ReviewManager> {
        FakeReviewManager(androidContext())
    }

    /* Dst auth service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_SECURE)
                .client(httpClient)
                .build()
                .createMockService<DstAuthService, DstAuthServiceMock>()
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .client(httpClient)
                .build()
                .createMockService<DstRecordService, DstRecordServiceMock>()
    }

    /* Dst enrollment service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .client(httpClient)
                .build()
                .createMockService<DstEnrollmentService, DstEnrollmentServiceMock>()
    }

    /* View Models */

    viewModel<MainViewModel>()
    viewModel<SplashViewModel>()
    viewModel<SummaryViewModel>()
    viewModel<RecordViewModel>()
    viewModel<SignInViewModel>()
    viewModel<ResetPasswordViewModel>()
    viewModel<UpdatePasswordViewModel>()
    viewModel<SubjectViewModel>()
    viewModel<EvaluationViewModel>()
    viewModel<PensumViewModel>()

    /* Repositories */

    factoryBy<DstRepository, DstDataSource>()
    factoryBy<SettingsRepository, PreferencesDataSource>()
    factoryBy<StorageRepository<File>, LocalStorageDataSource>()
    factoryBy<RemoteStorageRepository, RemoteStorageMockDataSource>()
    factoryBy<AuthRepository, AuthMockDataSource>()
    factoryBy<DatabaseRepository, FirestoreMockDataSource>()
    factoryBy<MessagingRepository, MessagingMockDataSource>()
    factoryBy<ContentRepository, ContentResolverDataSource>()
    factoryBy<LinkRepository, DynamicLinkDataSource>()
    factoryBy<ConfigRepository, RemoteConfigMockDataSource>()
    factoryBy<ReportingRepository, DebugReportingDataSource>()
    factoryBy<DependenciesRepository, DebugKoinDataSource>()
    factoryBy<NetworkRepository, AndroidNetworkDataSource>()

    /* Use cases */

    factory<SignInUseCase>()
    factory<SignOutUseCase>()
    factory<SyncAccountUseCase>()
    factory<StartUpUseCase>()
    factory<SendResetPasswordEmailUseCase>()
    factory<UpdatePasswordUseCase>()
    factory<CountdownUseCase>()
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

    /* Utils */

    single {
        Picasso.get()
    }
}