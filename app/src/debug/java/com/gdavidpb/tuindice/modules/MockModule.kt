package com.gdavidpb.tuindice.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.crashlytics.DebugReportingDataStore
import com.gdavidpb.tuindice.data.source.dynamic.DynamicLinkDataStore
import com.gdavidpb.tuindice.data.source.service.*
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataStore
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataStore
import com.gdavidpb.tuindice.data.source.storage.DiskStorageDataStore
import com.gdavidpb.tuindice.datastores.*
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.services.DstAuthServiceMock
import com.gdavidpb.tuindice.services.DstEnrollmentServiceMock
import com.gdavidpb.tuindice.services.DstRecordServiceMock
import com.gdavidpb.tuindice.utils.KEY_TIME_OUT_CONNECTION
import com.gdavidpb.tuindice.utils.KEY_TIME_SYNCHRONIZATION
import com.gdavidpb.tuindice.utils.createMockService
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.gdavidpb.tuindice.utils.isEmulator
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
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val mockModule = module {

    val localhost = if (isEmulator())
        BuildConfig.URL_EMULATOR_MOCK
    else
        BuildConfig.URL_LOCAL_MOCK

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
                .setHost("$localhost:8080")
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

    single {
        val syncTime = get<ConfigRepository>().getLong(KEY_TIME_SYNCHRONIZATION)

        DstAuthInterceptor(syncTime)
    }

    factory {
        val connectionTimeout = get<ConfigRepository>().getLong(KEY_TIME_OUT_CONNECTION)

        OkHttpClient.Builder()
                .callTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .hostnameVerifier(get<DstHostNameVerifier>())
                .cookieJar(get<DstCookieJar>())
                .addInterceptor(get<HttpLoggingInterceptor>())
    }

    factory {
        Retrofit.Builder()
                .addConverterFactory(JspoonConverterFactory.create())
    }

    /* Dst auth service */

    single {
        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_SECURE)
                .build()
                .createMockService<DstAuthService, DstAuthServiceMock>()
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .build()
                .createMockService<DstRecordService, DstRecordServiceMock>()
    }

    /* Dst enrollment service */

    single {
        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .build()
                .createMockService<DstEnrollmentService, DstEnrollmentServiceMock>()
    }

    /* View Models */

    viewModel<MainViewModel>()
    viewModel<SummaryViewModel>()
    viewModel<RecordViewModel>()
    viewModel<LoginViewModel>()
    viewModel<EmailSentViewModel>()
    viewModel<SubjectViewModel>()
    viewModel<PensumViewModel>()

    /* Factories */

    factoryBy<DstRepository, DstDataStore>()
    factoryBy<SettingsRepository, PreferencesDataStore>()
    factoryBy<LocalStorageRepository, DiskStorageDataStore>()
    factoryBy<RemoteStorageRepository, RemoteStorageMockDataStore>()
    factoryBy<AuthRepository, AuthMockDataStore>()
    factoryBy<DatabaseRepository, FirestoreMockDataStore>()
    factoryBy<IdentifierRepository, TokenMockDataStore>()
    factoryBy<ContentRepository, ContentResolverDataStore>()
    factoryBy<LinkRepository, DynamicLinkDataStore>()
    factoryBy<ConfigRepository, RemoteConfigMockDataStore>()
    factoryBy<ReportingRepository, DebugReportingDataStore>()

    /* Use cases */

    factory<SignInUseCase>()
    factory<SignOutUseCase>()
    factory<SyncAccountUseCase>()
    factory<StartUpUseCase>()
    factory<ResendVerifyEmailUseCase>()
    factory<ResendResetEmailUseCase>()
    factory<CountdownUseCase>()
    factory<GetProfileUseCase>()
    factory<GetQuartersUseCase>()
    factory<UpdateSubjectUseCase>()
    factory<SetLastScreenUseCase>()
    factory<OpenEnrollmentProofUseCase>()
    factory<GetSubjectEvaluationsUseCase>()
    factory<UpdateEvaluationUseCase>()
    factory<RemoveEvaluationUseCase>()
    factory<AddEvaluationUseCase>()
    factory<UpdateProfilePictureUseCase>()
    factory<CreateProfilePictureFileUseCase>()
    factory<GetProfilePictureFileUseCase>()
    factory<GetProfilePictureUseCase>()
    factory<RemoveProfilePictureUseCase>()

    /* Utils */

    single {
        Picasso.get()
    }
}