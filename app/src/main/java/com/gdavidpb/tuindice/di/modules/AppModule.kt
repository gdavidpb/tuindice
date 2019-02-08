package com.gdavidpb.tuindice.di.modules

import android.content.Context
import android.net.ConnectivityManager
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.mapper.*
import com.gdavidpb.tuindice.data.source.firebase.FirebaseDataStore
import com.gdavidpb.tuindice.data.source.firestore.FirestoreDataStore
import com.gdavidpb.tuindice.data.source.service.*
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataStore
import com.gdavidpb.tuindice.data.source.storage.DiskStorageDataStore
import com.gdavidpb.tuindice.data.source.storage.FirebaseStorageDataStore
import com.gdavidpb.tuindice.data.source.token.TokenDataStore
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentActivityViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.EnrollmentFragmentViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.LoginActivityViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainActivityViewModel
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_ENROLLMENT
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_RECORD
import com.gdavidpb.tuindice.utils.ENDPOINT_DST_SECURE
import com.gdavidpb.tuindice.utils.getProperty
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.defaultSharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module
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
        androidContext().resources
    }

    single {
        androidContext().defaultSharedPreferences
    }

    /* View Models */

    viewModel<MainActivityViewModel>()
    viewModel<LoginActivityViewModel>()
    viewModel<EnrollmentFragmentViewModel>()
    viewModel<EmailSentActivityViewModel>()

    /* Android Services */

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    single<DstHostNameVerifier>()

    single<DstCookieJar>()

    single<DstAuthInterceptor>()

    factory {
        OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .hostnameVerifier(get<DstHostNameVerifier>())
                .cookieJar(get<DstCookieJar>())
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
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
                .baseUrl(ENDPOINT_DST_SECURE)
                .build()
                .create(DstAuthService::class.java) as DstAuthService
    }

    /* Dst record service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(ENDPOINT_DST_RECORD)
                .build()
                .create(DstRecordService::class.java) as DstRecordService
    }

    /* Dst enrollment service */

    single {
        val httpClient = get<OkHttpClient.Builder>()
                .build()

        get<Retrofit.Builder>()
                .client(httpClient)
                .baseUrl(ENDPOINT_DST_ENROLLMENT)
                .build()
                .create(DstEnrollmentService::class.java) as DstEnrollmentService
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

    /* Factories */

    factoryBy<DstRepository, DstDataStore>()

    factoryBy<SettingsRepository, PreferencesDataStore>()

    factoryBy<LocalStorageRepository, DiskStorageDataStore>()

    factoryBy<RemoteStorageRepository, FirebaseStorageDataStore>()

    factoryBy<AuthRepository, FirebaseDataStore>()

    factoryBy<DatabaseRepository, FirestoreDataStore>()

    factoryBy<IdentifierRepository, TokenDataStore>()

    /* Mappers */

    factory<AuthResponseMapper>()

    factory<PeriodMapper>()

    factory<SubjectMapper>()

    factory<EnrollmentMapper>()

    factory<AccountSelectorMapper>()

    factory<QuarterMapper>()

    factory<RecordMapper>()

    factory<UsbIdMapper>()

    factory<ResetMapper>()

    factory<ResetParamMapper>()

    factory<CredentialsMapper>()

    /* Use cases */

    factory<LoginUseCase>()

    factory<LogoutUseCase>()

    factory<GetAccountUseCase>()

    factory<StartUpUseCase>()

    factory<ResetUseCase>()

    factory<ResendVerifyEmailUseCase>()

    factory<ResendResetEmailUseCase>()

    factory<CountdownUseCase>()
}