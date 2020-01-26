package com.gdavidpb.tuindice.di.modules

import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.source.firebase.FirebaseDataStore
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataStore
import com.gdavidpb.tuindice.data.source.service.*
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataStore
import com.gdavidpb.tuindice.data.source.storage.DiskStorageDataStore
import com.gdavidpb.tuindice.data.source.storage.FirebaseStorageDataStore
import com.gdavidpb.tuindice.data.source.token.TokenDataStore
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.*
import com.gdavidpb.tuindice.utils.TIME_OUT_CONNECTION
import com.gdavidpb.tuindice.utils.extensions.getProperty
import com.gdavidpb.tuindice.utils.extensions.noSensitiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.defaultSharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.experimental.dsl.viewModel
import org.koin.dsl.module
import org.koin.experimental.builder.factory
import org.koin.experimental.builder.factoryBy
import org.koin.experimental.builder.single
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val appModule = module {

    /* Application */

    single {
        androidContext().defaultSharedPreferences
    }

    /* Android Services */

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    single {
        androidContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    /* Firebase */

    single {
        FirebaseStorage.getInstance()
    }

    single {
        FirebaseFirestore.getInstance()
    }

    single {
        FirebaseAnalytics.getInstance(get())
    }

    single {
        FirebaseAuth.getInstance()
    }

    single {
        FirebaseInstanceId.getInstance()
    }

    /* SSL */

    single {
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    }

    single {
        get<TrustManagerFactory>().trustManagers.first { trustManager ->
            trustManager is X509TrustManager
        } as X509TrustManager
    }

    single {
        val sslContext = SSLContext.getInstance("TLS")

        val inputStream = androidContext().resources.openRawResource(R.raw.certificates)
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = certificateFactory.generateCertificates(inputStream)
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        inputStream.close()

        keyStore.load(null)

        certificates.forEach { certificate ->
            val alias = (certificate as X509Certificate).getProperty("CN") ?: "${UUID.randomUUID()}"

            keyStore.setCertificateEntry(alias, certificate)
        }

        get<TrustManagerFactory>().also { trustManagerFactory ->
            trustManagerFactory.init(keyStore)
            sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
        }

        sslContext
    }

    single {
        val logger = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Logger.DEFAULT
        } else {
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Crashlytics.log(message.noSensitiveData())
                }
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

    /* Utils */

    single {
        Picasso.get()
    }
}