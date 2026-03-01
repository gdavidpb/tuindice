package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository

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

class FakeNetworkRepository(
	private val isAvailable: Boolean = true
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
}

class RecordingReportingRepository : ReportingRepository {
	var lastIdentifier: String? = null
	val loggedExceptions = mutableListOf<Throwable>()
	val loggedMessages = mutableListOf<String>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		lastIdentifier = identifier
	}

	override fun logException(throwable: Throwable) {
		loggedExceptions += throwable
	}

	override fun logMessage(message: String) {
		loggedMessages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class FakeFileRepository(
	private val output: PlatformFileRef = PlatformFileRef("/tmp/test-file"),
	private val canOpenResult: Boolean = true
) : FileRepository {
	var lastNameHint: String? = null
	var lastCanOpenFileRef: PlatformFileRef? = null

	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		lastNameHint = nameHint
		return output
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean {
		lastCanOpenFileRef = fileRef
		return canOpenResult
	}
}

class RecordingApplicationRepository(
	private val output: PlatformFileRef = PlatformFileRef("/tmp/test-file"),
	private val canOpenResult: Boolean = true
) : ApplicationRepository {
	var cleared = false
		private set
	var clearCalls = 0
		private set
	var lastNameHint: String? = null
	var lastCanOpenFileRef: PlatformFileRef? = null

	override suspend fun clearData() {
		cleared = true
		clearCalls++
	}

	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		lastNameHint = nameHint
		return output
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean {
		lastCanOpenFileRef = fileRef
		return canOpenResult
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
