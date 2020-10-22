package com.gdavidpb.tuindice.di.modules

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataStore
import com.gdavidpb.tuindice.data.source.crashlytics.CrashlyticsReportingDataStore
import com.gdavidpb.tuindice.data.source.dynamic.DynamicLinkDataStore
import com.gdavidpb.tuindice.data.source.firebase.FirebaseDataStore
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataStore
import com.gdavidpb.tuindice.data.source.service.*
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataStore
import com.gdavidpb.tuindice.data.source.storage.ContentResolverDataStore
import com.gdavidpb.tuindice.data.source.storage.DiskStorageDataStore
import com.gdavidpb.tuindice.data.source.storage.FirebaseStorageDataStore
import com.gdavidpb.tuindice.data.source.token.TokenDataStore
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.utils.KEY_DST_CERTIFICATES
import com.gdavidpb.tuindice.utils.KEY_TIME_OUT_CONNECTION
import com.gdavidpb.tuindice.utils.KEY_TIME_SYNCHRONIZATION
import com.gdavidpb.tuindice.utils.extensions.create
import com.gdavidpb.tuindice.utils.extensions.encryptedSharedPreferences
import com.gdavidpb.tuindice.utils.extensions.inflate
import com.gdavidpb.tuindice.utils.extensions.noSensitiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
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
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val appModule = module {

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
        FirebaseStorage.getInstance()
    }

    single {
        FirebaseFirestore.getInstance()
    }

    single {
        FirebaseAnalytics.getInstance(androidContext())
    }

    single {
        FirebaseAuth.getInstance()
    }

    single {
        FirebaseInstanceId.getInstance()
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
                return get()
            }

            private fun Array<X509Certificate>.checkValidity() {
                forEach { certificate -> certificate.checkValidity() }
            }
        }
    }

    /* Certificates */

    single {
        get<ConfigRepository>()
                .getString(KEY_DST_CERTIFICATES)
                .inflate()
                .let(::ByteArrayInputStream)
                .use { inputStream ->
                    CertificateFactory.getInstance("X.509")
                            .generateCertificates(inputStream)
                            .map { certificate -> certificate as X509Certificate }
                }.toTypedArray()
    }

    single {
        val logger = HttpLoggingInterceptor.Logger { message ->
            get<FirebaseCrashlytics>().log(message.noSensitiveData())
        }

        HttpLoggingInterceptor(logger).apply { level = HttpLoggingInterceptor.Level.BODY }
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
                .followSslRedirects(true)
                .sslSocketFactory(get<SSLContext>().socketFactory, get())
    }

    factory {
        Retrofit.Builder()
                .addConverterFactory(JspoonConverterFactory.create())
    }

    /* Dst auth service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .addInterceptor(get<DstAuthInterceptor>())
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_SECURE)
                .build()
                .create<DstAuthService>()
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .build()
                .create<DstRecordService>()
    }

    /* Dst enrollment service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_ENROLLMENT)
                .build()
                .create<DstEnrollmentService>()
    }

    /* View Models */

    viewModel<MainViewModel>()
    viewModel<SummaryViewModel>()
    viewModel<RecordViewModel>()
    viewModel<LoginViewModel>()
    viewModel<EmailViewModel>()
    viewModel<SubjectViewModel>()
    viewModel<PensumViewModel>()

    /* Factories */

    factoryBy<DstRepository, DstDataStore>()
    factoryBy<SettingsRepository, PreferencesDataStore>()
    factoryBy<LocalStorageRepository, DiskStorageDataStore>()
    factoryBy<RemoteStorageRepository, FirebaseStorageDataStore>()
    factoryBy<AuthRepository, FirebaseDataStore>()
    factoryBy<DatabaseRepository, FirestoreDataStore>()
    factoryBy<IdentifierRepository, TokenDataStore>()
    factoryBy<ContentRepository, ContentResolverDataStore>()
    factoryBy<LinkRepository, DynamicLinkDataStore>()
    factoryBy<ConfigRepository, RemoteConfigDataStore>()
    factoryBy<ReportingRepository, CrashlyticsReportingDataStore>()

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