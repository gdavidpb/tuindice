package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserGateway
import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoGateway
import com.gdavidpb.tuindice.base.domain.repository.ExternalActions
import com.gdavidpb.tuindice.base.domain.repository.FileGateway
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.IntegrityGateway
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.PushGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewGateway
import com.gdavidpb.tuindice.base.domain.repository.SecureStore
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateGateway
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.login.data.repository.KtorAuthApiApiDataRepository
import com.gdavidpb.tuindice.login.data.repository.KtorMessagingApiDataRepository
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingApiRepository as LoginMessagingApiRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository as LoginMessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.ui.resource.HostUiTexts
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.screen.IosBrowserScreenRenderer
import com.gdavidpb.tuindice.base.data.source.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class IosConfigValues(
	val timeoutMillis: Long = 90_000L,
	val contactEmail: String = "info@tuindice.app",
	val contactSubject: String = "TuIndice - Contacto",
	val loadingMessages: List<String> = listOf("Iniciando sesión…"),
	val updateStalenessDays: Int = 7,
	val syncsToSuggestReview: Int = 3
)

data class IosUiTextValues(
	val googleServicesUnavailableTitle: String = "Servicios no disponibles",
	val googleServicesUnavailableMessage: String = "Google Play Services no aplica en iOS.",
	val googleServicesUnavailableExit: String = "Salir",
	val externalResourceTitle: String = "Abrir recurso externo",
	val externalResourceMessage: String = "Estás a punto de abrir un enlace externo.",
	val externalResourceOpen: String = "Abrir",
	val externalResourceCancel: String = "Cancelar"
)

data class IosPlatformAttestation(
	val token: String,
	val keyId: String? = null,
	val provider: AttestationProvider = AttestationProvider.APP_ATTEST
)

interface IosPlatformBridge {
	suspend fun fetchRemoteConfig() {}
	fun remoteConfigString(key: String): String?
	fun remoteConfigStringList(key: String): List<String>?
	suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation?
	suspend fun pushToken(): String?
	suspend fun launchReview()
	suspend fun checkForUpdate(stalenessDays: Int): UpdateAction?
	suspend fun launchUpdate(action: UpdateAction)
	fun openUrl(url: String)
	fun openStorePage()
	fun openFile(fileRef: PlatformFileRef): Boolean
	fun canOpen(fileRef: PlatformFileRef): Boolean
	fun sendEmail(email: String, subject: String, text: String)
	fun shareText(subject: String, text: String)
	fun appVersionName(): String
	fun appVersionCode(): Long
	fun hasCamera(): Boolean
	fun isNetworkAvailable(): Boolean
	fun setUserIdentifier(identifier: String)
	fun logMessage(message: String)
	fun logException(throwable: Throwable)
	fun setCustomKey(key: String, value: String)
	fun restartDependencies()
	fun secureStoreContains(key: String): Boolean
	fun secureStoreGetString(key: String): String?
	fun secureStorePutString(key: String, value: String)
	fun secureStoreClear()
}

object DefaultIosPlatformBridge : IosPlatformBridge {
	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? {
		failMissingBridge(api = "requestAttestation")
	}

	override suspend fun pushToken(): String? {
		failMissingBridge(api = "pushToken")
	}

	override suspend fun launchReview() = Unit

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = null

	override suspend fun launchUpdate(action: UpdateAction) = Unit

	override fun openUrl(url: String) {
		val platformUrl = NSURL.URLWithString(url) ?: return
		UIApplication.sharedApplication.openURL(platformUrl)
	}

	override fun openStorePage() {
		openUrl(STORE_URL)
	}

	override fun openFile(fileRef: PlatformFileRef): Boolean {
		val platformUrl = fileRef.toPlatformUrl() ?: return false
		if (!UIApplication.sharedApplication.canOpenURL(platformUrl)) return false
		UIApplication.sharedApplication.openURL(platformUrl)
		return true
	}

	override fun canOpen(fileRef: PlatformFileRef): Boolean {
		val platformUrl = fileRef.toPlatformUrl() ?: return false
		return UIApplication.sharedApplication.canOpenURL(platformUrl)
	}

	override fun sendEmail(email: String, subject: String, text: String) {
		val encodedSubject = subject.toMailQueryValue()
		val encodedText = text.toMailQueryValue()

		openUrl("mailto:$email?subject=$encodedSubject&body=$encodedText")
	}

	override fun shareText(subject: String, text: String) {
		val topController = topViewController()
			?: run {
				sendEmail(email = "", subject = subject, text = text)
				return
			}

		val activityController = UIActivityViewController(
			activityItems = listOf(text),
			applicationActivities = null
		)

		topController.presentViewController(
			viewControllerToPresent = activityController,
			animated = true,
			completion = null
		)
	}

	override fun appVersionName(): String {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
			?.toString()
			?: "0.0.0"
	}

	override fun appVersionCode(): Long {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion")
			?.toString()
			?.toLongOrNull()
			?: 0L
	}

	override fun hasCamera(): Boolean {
		return UIImagePickerController.isSourceTypeAvailable(
			UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
		)
	}

	override fun isNetworkAvailable(): Boolean = true

	override fun setUserIdentifier(identifier: String) = Unit

	override fun logMessage(message: String) {
		NSLog(message)
	}

	override fun logException(throwable: Throwable) {
		NSLog(throwable.message ?: "Unknown iOS bridge exception")
	}

	override fun setCustomKey(key: String, value: String) = Unit

	override fun restartDependencies() {
		failMissingBridge(api = "restartDependencies")
	}

	override fun secureStoreContains(key: String): Boolean {
		failMissingBridge(api = "secureStoreContains")
	}

	override fun secureStoreGetString(key: String): String? {
		failMissingBridge(api = "secureStoreGetString")
	}

	override fun secureStorePutString(key: String, value: String) {
		failMissingBridge(api = "secureStorePutString")
	}

	override fun secureStoreClear() {
		failMissingBridge(api = "secureStoreClear")
	}

	private fun PlatformFileRef.toPlatformUrl(): NSURL? {
		val rawValue = value

		return when {
			rawValue.startsWith("file://") -> NSURL.URLWithString(rawValue)
			rawValue.contains("://") -> NSURL.URLWithString(rawValue)
			else -> NSURL.fileURLWithPath(rawValue)
		}
	}

	private fun String.toMailQueryValue(): String {
		return replace(" ", "%20")
			.replace("\n", "%0A")
			.replace("&", "%26")
	}

	private fun topViewController(): UIViewController? {
		val keyWindow = UIApplication.sharedApplication
			.windows
			.firstOrNull { window ->
				(window as? UIWindow)?.isKeyWindow() == true
			} as? UIWindow

		var current = keyWindow?.rootViewController

		while (current?.presentedViewController != null) {
			current = current.presentedViewController
		}

		return current
	}

	private const val STORE_URL = "itms-apps://apps.apple.com"

	private fun failMissingBridge(api: String): Nothing {
		error(
			"DefaultIosPlatformBridge cannot execute '$api'. " +
					"Inject a real iOS bridge from host app bootstrap."
		)
	}
}

data class IosPlatformConfig(
	val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy_policy.html",
		termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions.html",
		debug = false
	),
	val configValues: IosConfigValues = IosConfigValues(),
	val uiTextValues: IosUiTextValues = IosUiTextValues(),
	val bridge: IosPlatformBridge = DefaultIosPlatformBridge,
	val secureStore: SecureStoreDataSource? = null,
	val dataStore: DataStore<Preferences> = createIosDataStore()
)

fun iosPlatformModule(
	config: IosPlatformConfig = IosPlatformConfig()
): Module = module {
	/* Session */

	single<SecureStoreDataSource> {
		config.secureStore ?: IosBridgeSecureStoreDataSource(get<IosPlatformBridge>())
	}
	single<SecureStore> { get<SecureStoreDataSource>() }
	single<DataStore<Preferences>> { config.dataStore }
	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }

	/* Platform services */

	single<IosPlatformBridge> { config.bridge }
	single<HostUiTextProvider> { StaticHostUiTextProvider(config.uiTextValues) }
	single<IdentifierRepository> { UUIDIdentifierDataSource() }
	single<AppEnvironmentRepository> { IosAppEnvironmentDataSource(config.appEnvironment) }
	single<AppEnvironmentGateway> { get<AppEnvironmentRepository>() }
	single<ConfigRepository> {
		IosConfigDataSource(
			values = config.configValues,
			bridge = get<IosPlatformBridge>()
		)
	}
	single<ConfigGateway> { get<ConfigRepository>() }
	single<NetworkRepository> { IosNetworkDataSource(get<IosPlatformBridge>()) }
	single<NetworkStatusGateway> { get<NetworkRepository>() }
	single<DependenciesRepository> { IosDependenciesDataSource(get<IosPlatformBridge>()) }
	single<DeviceInfoGateway> { IosDeviceInfoGateway(get<IosPlatformBridge>()) }
	single<BrowserGateway> { IosBrowserGateway(get<IosPlatformBridge>()) }
	single<BrowserScreenRenderer> { IosBrowserScreenRenderer() }
	single<ExternalActions> { IosExternalActions(get<IosPlatformBridge>()) }
	single<ReviewGateway> { IosReviewGateway(get<IosPlatformBridge>()) }
	single<UpdateGateway> { IosUpdateGateway(get<IosPlatformBridge>()) }
	single<SettingsRepository> { IosSettingsDataSource(get<DataStore<Preferences>>()) }
	single<ApplicationRepository> {
		IosApplicationDataSource(
			dataStore = get<DataStore<Preferences>>(),
			secureStoreDataSource = get<SecureStoreDataSource>(),
			bridge = get<IosPlatformBridge>()
		)
	}
	single<FileGateway> { get<ApplicationRepository>() }
	single<ReportingRepository> { IosReportingDataSource(get<IosPlatformBridge>()) }
	single<ReportingGateway> { get<ReportingRepository>() }
	factory<LoginReportingRepository> { IosLoginReportingDataSource(get<IosPlatformBridge>()) }
	factory<LoginMessagingRepository> { IosLoginMessagingDataSource(get<IosPlatformBridge>()) }
	factory<AuthApiRepository> {
		IosLazyAuthApiRepository(
			httpClientProvider = { get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)) }
		)
	}
	factory<LoginMessagingApiRepository> {
		IosLazyMessagingApiRepository(
			httpClientProvider = { get<HttpClient>() }
		)
	}
	factory<MessagingRepository> {
		IosMessagingDataRepository(
			dataStore = get<DataStore<Preferences>>(),
			httpClientProvider = { get<HttpClient>() },
			bridge = get<IosPlatformBridge>()
		)
	}
	factory<PushGateway> { get<MessagingRepository>() }
	factory<AttestationRepository> {
		IosAttestationDataRepository(
			httpClientProvider = {
				get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
			},
			json = get<Json>(),
			bridge = get<IosPlatformBridge>()
		)
	}
	factory<IntegrityGateway> { get<AttestationRepository>() }

	/* Serialization + network */

	single {
		createSharedJson()
	}

	single(named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)) {
		createIosIdentityHttpClient(
			appEnvironmentRepository = get<AppEnvironmentGateway>(),
			configRepository = get<ConfigGateway>(),
			logger = IOS_KTOR_LOGGER,
			json = get<Json>(),
			userAgentValue = createIosUserAgent(get<IosPlatformBridge>())
		)
	}

	single {
		val bridge = get<IosPlatformBridge>()

			createSharedHttpClient(
				appEnvironmentRepository = get<AppEnvironmentGateway>(),
				configRepository = get<ConfigGateway>(),
				sessionRepository = get<SessionRepository>(),
				attestationRepositoryProvider = { get<IntegrityGateway>() },
				authApiRepositoryProvider = { get<AuthApiRepository>() },
				logger = IOS_KTOR_LOGGER,
				json = get<Json>(),
				userAgentValue = createIosUserAgent(bridge)
			)
		}
}

private class IosAppEnvironmentDataSource(
	private val environment: AppEnvironment
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = environment
}

private class StaticHostUiTextProvider(
	private val values: IosUiTextValues
) : HostUiTextProvider {
	override fun getValues(): HostUiTexts {
		return HostUiTexts(
			googleServicesUnavailableTitle = values.googleServicesUnavailableTitle,
			googleServicesUnavailableMessage = values.googleServicesUnavailableMessage,
			googleServicesUnavailableExit = values.googleServicesUnavailableExit,
			externalResourceTitle = values.externalResourceTitle,
			externalResourceMessage = values.externalResourceMessage,
			externalResourceOpen = values.externalResourceOpen,
			externalResourceCancel = values.externalResourceCancel
		)
	}
}

private class IosConfigDataSource(
	private val values: IosConfigValues,
	private val bridge: IosPlatformBridge
) : ConfigRepository {
	override suspend fun tryFetch() {
		runCatching { bridge.fetchRemoteConfig() }
	}

	override fun getTimeout(): Long {
		return bridge.remoteConfigString(IosRemoteConfigKeys.TIME_OUT_CONNECTION)
			?.toLongOrNull()
			?: values.timeoutMillis
	}

	override fun getContactEmail(): String {
		return bridge.remoteConfigString(IosRemoteConfigKeys.CONTACT_EMAIL)
			?: values.contactEmail
	}

	override fun getContactSubject(): String {
		return bridge.remoteConfigString(IosRemoteConfigKeys.CONTACT_SUBJECT)
			?: values.contactSubject
	}

	override fun getLoadingMessages(): List<String> {
		return bridge.remoteConfigStringList(IosRemoteConfigKeys.LOADING_MESSAGES)
			?: values.loadingMessages
	}

	override fun getTimeUpdateStalenessDays(): Int {
		return bridge.remoteConfigString(IosRemoteConfigKeys.TIME_UPDATE_STALENESS_DAYS)
			?.toLongOrNull()
			?.toInt()
			?: values.updateStalenessDays
	}

	override fun getSyncsToSuggestReview(): Int {
		return bridge.remoteConfigString(IosRemoteConfigKeys.SYNCS_TO_SUGGEST_REVIEW)
			?.toLongOrNull()
			?.toInt()
			?: values.syncsToSuggestReview
	}
}

private class IosNetworkDataSource(
	private val bridge: IosPlatformBridge
) : NetworkRepository {
	override fun isAvailable(): Boolean = bridge.isNetworkAvailable()
}

private class IosDependenciesDataSource(
	private val bridge: IosPlatformBridge
) : DependenciesRepository {
	override fun restart() {
		if (restartIosKoinModules()) {
			return
		}

		bridge.restartDependencies()
	}
}

private class IosDeviceInfoGateway(
	private val bridge: IosPlatformBridge
) : DeviceInfoGateway {
	override fun appVersionName(): String = bridge.appVersionName()

	override fun appVersionCode(): Long = bridge.appVersionCode()

	override fun hasCamera(): Boolean = bridge.hasCamera()
}

private class IosBrowserGateway(
	private val bridge: IosPlatformBridge
) : BrowserGateway {
	override fun open(url: String) {
		bridge.openUrl(url)
	}
}

private class IosExternalActions(
	private val bridge: IosPlatformBridge
) : ExternalActions {
	override fun openFile(fileRef: PlatformFileRef): Boolean {
		return bridge.openFile(fileRef)
	}

	override fun sendEmail(email: String, subject: String, text: String) {
		bridge.sendEmail(email, subject, text)
	}

	override fun shareText(subject: String, text: String) {
		bridge.shareText(subject, text)
	}

	override fun openStorePage() {
		bridge.openStorePage()
	}
}

private class IosReviewGateway(
	private val bridge: IosPlatformBridge
) : ReviewGateway {
	override suspend fun launchReview() {
		bridge.launchReview()
	}
}

private class IosUpdateGateway(
	private val bridge: IosPlatformBridge
) : UpdateGateway {
	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		return bridge.checkForUpdate(stalenessDays)
	}

	override suspend fun launchUpdate(action: UpdateAction) {
		bridge.launchUpdate(action)
	}
}

private class IosSettingsDataSource(
	private val dataStore: DataStore<Preferences>
) : SettingsRepository {
	override suspend fun isReviewSuggested(value: Int): Boolean {
		val counter = (dataStore.data.first()[SYNCS_COUNTER] ?: 0) + 1

		dataStore.edit { preferences ->
			preferences[SYNCS_COUNTER] = counter
		}

		return counter == value
	}

	override suspend fun getLastDestination(): Destination {
		return dataStore.data.first()[LAST_DESTINATION]
			?.toDestination()
			?: SummaryDestination.NavGraph
	}

	override suspend fun setLastDestination(destination: Destination) {
		dataStore.edit { preferences ->
			preferences[LAST_DESTINATION] = destination.toDestinationName()
		}
	}

	override suspend fun clear() {
		dataStore.edit { preferences ->
			preferences.clear()
		}
	}

	companion object {
		private val LAST_DESTINATION = stringPreferencesKey(PreferencesKeys.LAST_DESTINATION)
		private val SYNCS_COUNTER = intPreferencesKey(PreferencesKeys.SYNCS_COUNTER)
	}
}

private class IosApplicationDataSource(
	private val dataStore: DataStore<Preferences>,
	private val secureStoreDataSource: SecureStoreDataSource,
	private val bridge: IosPlatformBridge
) : ApplicationRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		val storagePath = temporaryStorageRoot().resolve(nameHint)

		FileSystem.SYSTEM.createDirectories(storagePath.parent!!)
		FileSystem.SYSTEM.delete(storagePath, mustExist = false)
		FileSystem.SYSTEM.write(storagePath) {}

		return PlatformFileRef(storagePath.toString())
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean {
		return bridge.canOpen(fileRef)
	}

	override suspend fun clearData() {
		dataStore.edit { preferences ->
			preferences.clear()
		}

		secureStoreDataSource.clear()

		runCatching {
			FileSystem.SYSTEM.deleteRecursively(temporaryStorageRoot(), mustExist = false)
		}
	}
}

private class IosReportingDataSource(
	private val bridge: IosPlatformBridge
) : ReportingRepository {
	override fun setIdentifier(identifier: String) {
		bridge.setUserIdentifier(identifier)
	}

	override fun logException(throwable: Throwable) {
		bridge.logException(throwable)
	}

	override fun logMessage(message: String) {
		bridge.logMessage(message)
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		bridge.setCustomKey(key, value.toString())
	}
}

private class IosLoginReportingDataSource(
	private val bridge: IosPlatformBridge
) : LoginReportingRepository {
	override suspend fun setIdentifier(id: String) {
		bridge.setUserIdentifier(id)
	}
}

private class IosLoginMessagingDataSource(
	private val bridge: IosPlatformBridge
) : LoginMessagingRepository {
	override suspend fun getToken(): String {
		return bridge.pushToken()
			?.takeIf { token -> token.isNotBlank() }
		?: throw IllegalStateException("Push token unavailable on iOS bridge.")
	}
}

private class IosLazyAuthApiRepository(
	private val httpClientProvider: () -> HttpClient
) : AuthApiRepository {
	private fun delegate(): AuthApiRepository {
		return KtorAuthApiApiDataRepository(httpClientProvider())
	}

	override suspend fun issueTokens(
		usbId: String,
		password: String,
		attestation: Attestation
	) = delegate().issueTokens(
		usbId = usbId,
		password = password,
		attestation = attestation
	)

	override suspend fun refreshTokens(
		accessToken: String,
		refreshToken: String,
		attestation: Attestation
	) = delegate().refreshTokens(
		accessToken = accessToken,
		refreshToken = refreshToken,
		attestation = attestation
	)

	override suspend fun revokeTokens() {
		delegate().revokeTokens()
	}
}

private class IosLazyMessagingApiRepository(
	private val httpClientProvider: () -> HttpClient
) : LoginMessagingApiRepository {
	override suspend fun subscribe(token: String) {
		KtorMessagingApiDataRepository(httpClientProvider()).subscribe(token)
	}
}

private class IosMessagingDataRepository(
	private val dataStore: DataStore<Preferences>,
	private val httpClientProvider: () -> HttpClient,
	private val bridge: IosPlatformBridge
) : MessagingRepository {
	override suspend fun subscribe() {
		val httpClient = httpClientProvider()
		val token = bridge.pushToken()
			?.takeIf { value -> value.isNotBlank() }
			?: return
		val preferences = dataStore.data.first()
		val hasMatchingToken = preferences[IS_SUBSCRIBED] == true &&
				preferences[PUSH_TOKEN] == token

		if (hasMatchingToken) return

		httpClient.post("messaging") {
			setBody(SubscribeRequest(token))
		}

		dataStore.edit { preferences ->
			preferences[IS_SUBSCRIBED] = true
			preferences[PUSH_TOKEN] = token
		}
	}

	override suspend fun unsubscribe() {
		val httpClient = httpClientProvider()
		runCatching {
			httpClient.delete("messaging")
		}

		dataStore.edit { preferences ->
			preferences.remove(IS_SUBSCRIBED)
			preferences.remove(PUSH_TOKEN)
		}
	}

	companion object {
		private val IS_SUBSCRIBED = booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)
		private val PUSH_TOKEN = stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)
	}
}

private class IosAttestationDataRepository(
	private val httpClientProvider: () -> HttpClient,
	private val json: Json,
	private val bridge: IosPlatformBridge
) : AttestationRepository {
	@OptIn(ExperimentalEncodingApi::class)
	override suspend fun getAttestation(payload: AttestationPayload): Attestation {
		val httpClient = httpClientProvider()
		val challenge = httpClient
			.get("auth/challenge")
			.body<ChallengeResponse>()

		val noncePayload = buildJsonObject {
			put("payload", json.encodeToJsonElement(payload))
			put("challenge", json.encodeToJsonElement(challenge.challenge))
		}.toString()

		val attestationInput = Base64.UrlSafe.encode(noncePayload.encodeToByteArray())

		val providerAttestation = bridge.requestAttestation(attestationInput)
			?: throw IllegalStateException("Attestation token unavailable on iOS bridge.")
		check(providerAttestation.provider == AttestationProvider.APP_ATTEST) {
			"Unsupported iOS attestation provider: ${providerAttestation.provider}."
		}
		val keyId = providerAttestation.keyId
			?.takeIf { value -> value.isNotBlank() }
			?: throw IllegalStateException(
				"Attestation keyId unavailable for iOS APP_ATTEST provider."
			)

		return Attestation(
			id = challenge.id,
			token = providerAttestation.token,
			provider = providerAttestation.provider,
			keyId = keyId
		)
	}
}

private class IosBridgeSecureStoreDataSource(
	private val bridge: IosPlatformBridge
) : SecureStoreDataSource {
	override fun contains(key: String): Boolean {
		return bridge.secureStoreContains(key)
	}

	override fun getString(key: String): String? {
		return bridge.secureStoreGetString(key)
	}

	override fun putString(key: String, value: String) {
		bridge.secureStorePutString(key, value)
	}

	override fun clear() {
		bridge.secureStoreClear()
	}
}

@Serializable
private data class ChallengeResponse(
	@SerialName("id") val id: String,
	@SerialName("challenge") val challenge: String
)

@Serializable
private data class SubscribeRequest(
	@SerialName("token") val token: String
)

private object IosRemoteConfigKeys {
	const val CONTACT_EMAIL = "contact_email"
	const val CONTACT_SUBJECT = "contact_subject"
	const val LOADING_MESSAGES = "loading_messages"
	const val TIME_UPDATE_STALENESS_DAYS = "time_update_staleness_days"
	const val SYNCS_TO_SUGGEST_REVIEW = "syncs_to_suggest_review"
	const val TIME_OUT_CONNECTION = "time_out_connection"
}

internal fun createIosUserAgent(bridge: IosPlatformBridge): String {
	val appVersionName = bridge.appVersionName().ifBlank { "0.0.0" }
	val appVersionCode = bridge.appVersionCode().coerceAtLeast(0L)
	val device = UIDevice.currentDevice
	val osVersion = device.systemVersion.ifBlank { "Unknown" }
	val osCode = osVersion
		.substringBefore(".")
		.toIntOrNull()
		?: 0
	val osId = NSBundle.mainBundle.objectForInfoDictionaryKey("DTPlatformBuild")
		?.toString()
		?.takeIf { it.isNotBlank() }
		?: "Unknown"
	val model = device.model.takeIf { it.isNotBlank() } ?: "Unknown"

	return buildStructuredUserAgent(
		appVersionName = appVersionName,
		appVersionCode = appVersionCode,
		osName = "iOS",
		osVersion = osVersion,
		osCode = osCode,
		osId = osId,
		manufacturer = "Apple",
		model = model
	)
}

private fun createIosIdentityHttpClient(
	appEnvironmentRepository: AppEnvironmentGateway,
	configRepository: ConfigGateway,
	logger: Logger,
	json: Json,
	userAgentValue: String?
): HttpClient {
	return createPlatformHttpClient {
		expectSuccess = true

		install(DefaultRequest) {
			val appEnvironment = appEnvironmentRepository.getEnvironment()

			url(appEnvironment.apiBaseUrl)
			contentType(ContentType.Application.Json)

			if (!userAgentValue.isNullOrBlank()) {
				userAgent(userAgentValue)
			}
		}

		install(HttpTimeout) {
			val timeout = configRepository.getTimeout()

			requestTimeoutMillis = timeout
			connectTimeoutMillis = timeout
			socketTimeoutMillis = timeout
		}

		install(ContentNegotiation) {
			json(json)
		}

		install(Logging) {
			this.logger = logger
			level = LogLevel.ALL

			sanitizeHeader { header ->
				header == HttpHeaders.Authorization
			}
		}
	}
}

internal const val IOS_IDENTITY_HTTP_CLIENT_QUALIFIER = "iosIdentityHttpClient"
private val IOS_KTOR_LOGGER = object : Logger {
	override fun log(message: String) {
		NSLog(message)
	}
}

private const val DEFAULT_DATASTORE_FILE_NAME = "tuindice.preferences_pb"

private fun createIosDataStore(
	fileName: String = DEFAULT_DATASTORE_FILE_NAME
): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(
		scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
		produceFile = {
			val path = persistentStorageRoot().resolve(fileName)
			FileSystem.SYSTEM.createDirectories(path.parent!!)
			path
		}
	)
}

private fun temporaryStorageRoot(): Path {
	return "${NSTemporaryDirectory().trimEnd('/')}/tuindice".toPath()
}

private fun persistentStorageRoot(): Path {
	return "${NSHomeDirectory().trimEnd('/')}/Library/Application Support/tuindice".toPath()
}

private fun Destination.toDestinationName(): String = when (this) {
	is SummaryDestination.NavGraph -> "summary"
	is RecordDestination.NavGraph -> "record"
	is EvaluationsDestination.NavGraph -> "evaluations"
	is AboutDestination.NavGraph -> "about"
	else -> "summary"
}

private fun String.toDestination(): Destination = when (this) {
	"summary" -> SummaryDestination.NavGraph
	"record" -> RecordDestination.NavGraph
	"evaluations" -> EvaluationsDestination.NavGraph
	"about" -> AboutDestination.NavGraph
	else -> SummaryDestination.NavGraph
}
