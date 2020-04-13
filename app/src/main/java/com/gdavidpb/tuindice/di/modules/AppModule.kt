package com.gdavidpb.tuindice.di.modules

import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.crashlytics.CrashlyticsReportingDataStore
import com.gdavidpb.tuindice.data.source.crashlytics.DebugReportingDataStore
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
import com.gdavidpb.tuindice.utils.TIME_OUT_CONNECTION
import com.gdavidpb.tuindice.utils.extensions.noSensitiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
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
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val appModule = module {

    /* Application */

    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }

    /* Android Services */

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    single {
        androidContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    single {
        androidContext().contentResolver
    }

    /* Firebase */

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
        Crashlytics.getInstance()
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
        androidContext().resources.openRawResource(R.raw.certificates).use { inputStream ->
            CertificateFactory.getInstance("X.509")
                    .generateCertificates(inputStream)
                    .map { certificate -> certificate as X509Certificate }
        }.toTypedArray()
    }

    single {
        val logger = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Logger.DEFAULT
        else object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Crashlytics.log(message.noSensitiveData())
            }
        }

        HttpLoggingInterceptor(logger).apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    single<DstHostNameVerifier>()

    single<DstCookieJar>()

    single<DstAuthInterceptor>()

    factory {
        OkHttpClient.Builder()
                .callTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
                .connectTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT_CONNECTION, TimeUnit.MILLISECONDS)
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
                .create(DstAuthService::class.java) as DstAuthService
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_RECORD)
                .build()
                .create(DstRecordService::class.java) as DstRecordService
    }

    /* Dst enrollment service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(BuildConfig.ENDPOINT_DST_ENROLLMENT)
                .build()
                .create(DstEnrollmentService::class.java) as DstEnrollmentService
    }

    /* View Models */

    viewModel<MainViewModel>()
    viewModel<SummaryViewModel>()
    viewModel<RecordViewModel>()
    viewModel<LoginViewModel>()
    viewModel<EmailSentViewModel>()
    viewModel<SubjectViewModel>()

    /* Factories */

    factoryBy<DstRepository, DstDataStore>()
    factoryBy<SettingsRepository, PreferencesDataStore>()
    factoryBy<LocalStorageRepository, DiskStorageDataStore>()
    factoryBy<RemoteStorageRepository, FirebaseStorageDataStore>()
    factoryBy<AuthRepository, FirebaseDataStore>()
    factoryBy<DatabaseRepository, FirestoreDataStore>()
    factoryBy<IdentifierRepository, TokenDataStore>()
    factoryBy<ContentRepository, ContentResolverDataStore>()

    factory {
        if (BuildConfig.DEBUG)
            DebugReportingDataStore()
        else
            CrashlyticsReportingDataStore()
    }

    /* Use cases */

    factory<SignInUseCase>()
    factory<SignOutUseCase>()
    factory<SyncAccountUseCase>()
    factory<StartUpUseCase>()
    factory<ResendVerifyEmailUseCase>()
    factory<ResendResetEmailUseCase>()
    factory<CountdownUseCase>()
    factory<GetAccountUseCase>()
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

    /* Utils */

    single {
        Picasso.get()
    }
}