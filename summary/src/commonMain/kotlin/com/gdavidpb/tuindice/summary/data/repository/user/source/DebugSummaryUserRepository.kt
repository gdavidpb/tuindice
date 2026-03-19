package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataSource
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_LOAD_DELAY_MILLIS = 900L
private const val DEFAULT_PICTURE_OPERATION_DELAY_MILLIS = 1_200L
private const val SUMMARY_DEBUG_LOG_TAG = "SummaryDebug"

val DEFAULT_DEBUG_SUMMARY_USER = User(
	id = "debug-user-1",
	cid = "debug-cid-1",
	usbId = "20990001",
	email = "debug@tuindice.app",
	pictureUrl = "",
	fullName = "Usuario Debug",
	firstNames = "Usuario",
	lastNames = "Debug",
	careerName = "Ingenieria Civil Informatica",
	careerCode = 14056,
	scholarship = false,
	grade = 5.12,
	enrolledSubjects = 6,
	enrolledCredits = 28,
	approvedSubjects = 34,
	approvedCredits = 164,
	retiredSubjects = 1,
	retiredCredits = 4,
	failedSubjects = 2,
	failedCredits = 8,
	lastUpdate = 1_709_251_200_000L
)

class DebugSummaryUserRepository(
	private val localDataSource: LocalDataSource,
	private val sourceName: String,
	private val initialUser: User = DEFAULT_DEBUG_SUMMARY_USER,
	private val loadDelayMillis: Long = DEFAULT_LOAD_DELAY_MILLIS,
	private val pictureOperationDelayMillis: Long = DEFAULT_PICTURE_OPERATION_DELAY_MILLIS
) : UserRepository {
	private val logger = appLogger(SUMMARY_DEBUG_LOG_TAG)
	private val initializationMutex = Mutex()
	private var isInitialized = false

	override suspend fun observeUserFlow(): Flow<User> {
		return localDataSource.getUserFlow()
			.mapNotNull { user -> user }
			.distinctUntilChanged()
	}

	override suspend fun updateUser() {
		ensureInitialized()
	}

	override suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture {
		ensureInitialized()

		val localPicturePath = file.path
		logger.i {
			"[$sourceName] uploadProfilePicture(): returning selected local picture path instead of remote URL."
		}

		delay(pictureOperationDelayMillis)
		localDataSource.saveProfilePicture(url = localPicturePath)

		return ProfilePicture(url = localPicturePath)
	}

	override suspend fun removeProfilePicture() {
		ensureInitialized()

		logger.i { "[$sourceName] removeProfilePicture(): clearing mocked profile picture." }

		delay(pictureOperationDelayMillis)
		localDataSource.saveProfilePicture(url = "")
	}

	private suspend fun ensureInitialized() {
		if (isInitialized) return

		initializationMutex.withLock {
			if (isInitialized) return@withLock

			val existingUser = localDataSource.getUserFlow().first()
			val seededUser = initialUser.copy(
				pictureUrl = existingUser?.pictureUrl.orEmpty()
			)

			logger.i { "[$sourceName] updateUser(): seeding debug summary user." }

			delay(loadDelayMillis)
			localDataSource.saveUser(user = seededUser)
			isInitialized = true
		}
	}
}
