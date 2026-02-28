package com.gdavidpb.tuindice.about.testing

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.presentation.Mutation

class FakeAboutRepository(
	private val versionDescription: String = "Producción v3.5.1 (351)"
) : AboutRepository {
	override suspend fun getVersionDescription(): String = versionDescription
}

class FakeEnvironmentDataSource(
	private val isDebug: Boolean
) : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean = isDebug
}

class FakeAppInfoDataSource(
	private val versionName: String = "3.5.1",
	private val versionCode: Long = 351L
) : AppInfoDataSource {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode
}

class FakeStoreUrlDataSource(
	private val storeUrl: String = "market://details?id=com.gdavidpb.tuindice"
) : StoreUrlDataSource {
	override fun getStoreUrl(): String = storeUrl
}

class FakeAppEnvironmentRepository(
	private val appEnvironment: AppEnvironment = AppEnvironment(
		apiBaseUrl = "https://api.tuindice.app/",
		privacyPolicyUrl = "https://tuindice.app/privacy",
		termsAndConditionsUrl = "https://tuindice.app/terms",
		debug = false
	)
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}

class RecordingBrowserRepository : BrowserRepository {
	var lastOpenedUrl: String? = null

	override fun open(url: String) {
		lastOpenedUrl = url
	}
}

class FakeConfigRepository(
	private val email: String = "support@tuindice.app",
	private val subject: String = "Support TuIndice"
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = email

	override fun getContactSubject(): String = subject

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3
}

fun List<Mutation<About.State>>.reduceAboutState(
	initialState: About.State = About.State.Idle
): About.State {
	return fold(initialState) { state, mutation -> mutation(state) }
}
