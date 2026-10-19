package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataRepository
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.domain.usecase.ObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.SettingsTextProvider
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import com.gdavidpb.tuindice.about.ui.model.SettingsActions
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

const val CURRENT_APP_VERSION_NAME = "5.8"
const val CURRENT_APP_VERSION_CODE = 36L
const val CURRENT_PRODUCTION_VERSION_TEXT = "Producción v$CURRENT_APP_VERSION_NAME ($CURRENT_APP_VERSION_CODE)"

class FakeAboutRepository(
	private val versionDescription: String = CURRENT_PRODUCTION_VERSION_TEXT
) : AboutRepository {
	override suspend fun getVersionDescription(): String = versionDescription
}

class FakeEnvironmentDataSource(
	private val isDebug: Boolean
) : EnvironmentDataRepository {
	override fun isDebugEnvironment(): Boolean = isDebug
}

class FakeAppInfoDataSource(
	private val versionName: String = CURRENT_APP_VERSION_NAME,
	private val versionCode: Long = CURRENT_APP_VERSION_CODE
) : AppInfoDataRepository {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode
}

class FakeStoreUrlDataSource(
	private val storeUrl: String = "market://details?id=com.gdavidpb.tuindice"
) : StoreUrlRepository {
	override fun getStoreUrl(): String = storeUrl
}

// Fixed text, so a walk over the about machine resolves it on any host. The value names its key.
class FakeAboutTextProvider : AboutTextProvider {
	override suspend fun versionUnavailable(): String = "text:version-unavailable"
}

// Fixed texts, so a walk over the settings machine resolves them on any host. The values name their key.
class FakeSettingsTextProvider : SettingsTextProvider {
	override suspend fun versionUnavailable(): String = "text:version-unavailable"

	override suspend fun termsAndConditionsLabel(): String = "text:terms"

	override suspend fun privacyPolicyLabel(): String = "text:privacy"

	override suspend fun supportLabel(): String = "text:support"

	override suspend fun shareSubject(): String = "text:share-subject"

	override suspend fun shareMessage(): String = "text:share-message"

	override suspend fun assistantConsentFailed(): String = "text:consent-failed"

	override suspend fun assistantConversationsDeleted(): String = "text:conversations-deleted"

	override suspend fun assistantConversationsDeleteFailed(): String = "text:conversations-delete-failed"
}

val ANA_PROFILE_IDENTITY = ProfileIdentity(
	name = "Ana Diaz",
	careerName = "Ingenieria Civil Informatica",
	carnet = "20261234",
	pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
	pictureVersion = 2
)

// The identity the device holds: a person by default, or nobody when no user is stored.
class FakeProfileIdentityRepository(
	identity: ProfileIdentity? = ANA_PROFILE_IDENTITY
) : ProfileIdentityRepository {
	val identity = MutableStateFlow(identity)

	override fun observeProfileIdentity(): Flow<ProfileIdentity?> = identity
}

fun fakeObserveProfileIdentityUseCase(
	profileIdentityRepository: ProfileIdentityRepository = FakeProfileIdentityRepository()
): ObserveProfileIdentityUseCase {
	return ObserveProfileIdentityUseCase(
		profileIdentityRepository = profileIdentityRepository,
		reportingRepository = RecordingReportingRepository()
	)
}

// Settings callbacks that do nothing: a test replaces the ones it looks at with `copy`.
fun noSettingsActions() = SettingsActions(
	onXClick = {},
	onTermsAndConditionsClick = {},
	onPrivacyPolicyClick = {},
	onSupportClick = {},
	onShareAppClick = {},
	onRateOnPlayStoreClick = {},
	onReportBugClick = {},
	onAboutClick = {},
	onSignOutClick = {},
	onUsageDataCollectionEnabledChange = {},
	onAssistantConsentChange = {},
	onAssistantMemoryClick = {},
	onAssistantConversationsClick = {}
)

// About callbacks that do nothing: a test replaces the ones it looks at with `copy`.
fun noAboutActions() = AboutActions(
	onCreativeCommonsClick = {},
	onGithubClick = {},
	onKotlinClick = {},
	onComposeClick = {},
	onFirebaseClick = {},
	onKoinClick = {},
	onKtorClick = {},
	onDstClick = {},
	onContactDeveloperClick = {}
)
