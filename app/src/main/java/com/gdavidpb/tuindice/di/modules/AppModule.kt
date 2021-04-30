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
import com.gdavidpb.tuindice.data.source.dynamic.DynamicLinkDataSource
import com.gdavidpb.tuindice.data.source.firebase.FirebaseDataSource
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.service.*
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
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.gdavidpb.tuindice.utils.extensions.inflate
import com.gdavidpb.tuindice.utils.extensions.sharedPreferences
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
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
import java.io.ByteArrayInputStream
import java.io.File
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val appModule = module {

    /* Application */

    single {
        runCatching {
            androidContext().encryptedSharedPreferences()
        }.getOrDefault(androidContext().sharedPreferences())
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

    /* Firebase */

    single {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(R.xml.default_remote_config)
        }
    }

    single {
        FirebaseStorage.getInstance()
    }

    single {
        FirebaseFirestore.getInstance()
    }

    single {
        FirebaseAuth.getInstance()
    }

    single {
        FirebaseMessaging.getInstance()
    }

    single {
        FirebaseDynamicLinks.getInstance()
    }

    single {
        FirebaseCrashlytics.getInstance()
    }

    /* SSL context */

    single {
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(get<X509TrustManager>()), SecureRandom())
        }
    }

    /* X509 trust manager */

    single<X509TrustManager> {
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                chain.checkValidity()
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                chain.checkValidity()
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                val certificates = get<Array<X509Certificate>>()

                check(certificates.isNotEmpty()) { "certificates not loaded" }

                return certificates
            }

            private fun Array<X509Certificate>.checkValidity() {
                forEach { certificate -> certificate.checkValidity() }
            }
        }
    }

    /* Certificates */

    single {
        runCatching {
            get<ConfigRepository>()
                    .getString(ConfigKeys.DST_CERTIFICATES)
                    .inflate()
                    .let(::ByteArrayInputStream)
                    .use { inputStream ->
                        CertificateFactory.getInstance("X.509")
                                .generateCertificates(inputStream)
                                .map { certificate -> certificate as X509Certificate }
                    }.toTypedArray()
        }.getOrDefault(emptyArray())
    }

    single {
        val logger = HttpLoggingInterceptor.Logger { message ->
            get<ReportingRepository>().logMessage(message)
        }

        HttpLoggingInterceptor(logger).apply { level = HttpLoggingInterceptor.Level.BODY }
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
                .followSslRedirects(true)
                .sslSocketFactory(get<SSLContext>().socketFactory, get())
    }

    factory {
        ReviewManagerFactory.create(androidContext())
    }

    /* Dst auth service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .addInterceptor(get<DstAuthInterceptor>())
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_SECURE)
                .addConverterFactory(JspoonConverterFactory.create())
                .client(httpClient)
                .build()
                .create<DstAuthService>()
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .addConverterFactory(JspoonConverterFactory.create())
                .client(httpClient)
                .build()
                .create<DstRecordService>()
    }

    /* Dst enrollment service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_DST_ENROLLMENT)
                .addConverterFactory(JspoonConverterFactory.create())
                .client(httpClient)
                .build()
                .create<DstEnrollmentService>()
    }

    /* View Models */

    viewModel<MainViewModel>()
    viewModel<SplashViewModel>()
    viewModel<SummaryViewModel>()
    viewModel<RecordViewModel>()
    viewModel<SignInViewModel>()
    viewModel<ResetPasswordViewModel>()
    viewModel<UpdatePasswordViewModel>()
    viewModel<EvaluationPlanViewModel>()
    viewModel<EvaluationViewModel>()
    viewModel<PensumViewModel>()

    /* Repositories */

    factoryBy<DstRepository, DstDataSource>()
    factoryBy<SettingsRepository, PreferencesDataSource>()
    factoryBy<StorageRepository<File>, LocalStorageDataSource>()
    factoryBy<RemoteStorageRepository, FirebaseStorageDataSource>()
    factoryBy<AuthRepository, FirebaseDataSource>()
    factoryBy<DatabaseRepository, FirestoreDataSource>()
    factoryBy<MessagingRepository, FirebaseCloudMessagingDataSource>()
    factoryBy<ContentRepository, ContentResolverDataSource>()
    factoryBy<LinkRepository, DynamicLinkDataSource>()
    factoryBy<ConfigRepository, RemoteConfigDataSource>()
    factoryBy<ReportingRepository, CrashlyticsReportingDataSource>()
    factoryBy<DependenciesRepository, ReleaseKoinDataSource>()
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
    factory<GetUpdateInfoUseCase>()

    /* Utils */

    single {
        Picasso.get()
    }
}