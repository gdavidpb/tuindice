package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

val DEFAULT_SUMMARY_USER = User(
	id = "user-1",
	cid = "cid-1",
	usbId = "20261234",
	email = "ana@tuindice.app",
	pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
	fullName = "Ana Maria Diaz Soto",
	firstNames = "Ana Maria",
	lastNames = "Diaz Soto",
	careerName = "Ingenieria Civil Informatica",
	careerCode = 14056,
	scholarship = false,
	grade = 4.4138,
	enrolledSubjects = 5,
	enrolledCredits = 24,
	approvedSubjects = 32,
	approvedCredits = 156,
	retiredSubjects = 1,
	retiredCredits = 4,
	failedSubjects = 2,
	failedCredits = 8,
	lastUpdate = 1_709_251_200_000L
)

val DEFAULT_SUMMARY_PROFILE_PICTURE = ProfilePicture(
	url = "https://cdn.tuindice.app/profile/updated.jpg"
)

val DEFAULT_ENCODED_IMAGE = EncodedImage(
	content = byteArrayOf(1, 2, 3, 4),
	mimeType = "image/jpeg"
)

class RecordingUserRepository(
	private val users: Flow<User> = flowOf(DEFAULT_SUMMARY_USER),
	private val profilePicture: ProfilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE,
	private val throwable: Throwable? = null,
	private val updateThrowable: Throwable? = null
) : UserRepository {
	var updateCalls = 0
	val uploadCalls = mutableListOf<PlatformFile>()
	var removeCalls = 0

	override suspend fun observeUserFlow(): Flow<User> = users

	override suspend fun updateUser() {
		updateCalls++
		updateThrowable?.let { throw it }
	}

	override suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture {
		uploadCalls += file
		throwable?.let { throw it }
		return profilePicture
	}

	override suspend fun removeProfilePicture() {
		removeCalls++
		throwable?.let { throw it }
	}
}

class FakeLocalDataSource(
	initialUser: User? = DEFAULT_SUMMARY_USER
) : LocalDataRepository {
	private val userState = MutableStateFlow(initialUser)

	val savedUsers = mutableListOf<User>()

	override fun getUserFlow(): Flow<User?> = userState

	override suspend fun updateUser(user: User) {
		savedUsers += user
		userState.value = user
	}
}

class FakeRemoteDataSource(
	private val user: User = DEFAULT_SUMMARY_USER,
	private val profilePicture: ProfilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE,
	private val removeThrowable: Throwable? = null
) : RemoteDataRepository {
	var getUserCalls = 0
	val uploadCalls = mutableListOf<Pair<ByteArray, String>>()
	var removeCalls = 0

	override suspend fun getUser(): User {
		getUserCalls++
		return user
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		uploadCalls += content to mimeType
		return profilePicture
	}

	override suspend fun removeProfilePicture() {
		removeCalls++
		removeThrowable?.let { throw it }
	}
}

class FakeSettingsDataSource(
	private val onCooldown: Boolean
) : SettingsDataRepository {
	var cooldownMarked = false

	val profilePictureVersion = MutableStateFlow(0)

	override suspend fun isGetUserOnCooldown(): Boolean = onCooldown

	override suspend fun setGetUserOnCooldown() {
		cooldownMarked = true
	}

	override fun observeProfilePictureVersion(): Flow<Int> = profilePictureVersion

	override suspend fun bumpProfilePictureVersion() {
		profilePictureVersion.value += 1
	}
}

class FakePictureEncoderDataSource(
	private val encodedImage: EncodedImage = DEFAULT_ENCODED_IMAGE,
	private val throwable: Throwable? = null
) : PictureEncoderDataRepository {
	var lastFile: PlatformFile? = null

	override suspend fun encodePicture(file: PlatformFile): EncodedImage {
		lastFile = file
		throwable?.let { throw it }
		return encodedImage
	}
}

class FakeProfilePictureInputDataSource(
	private val normalizedFile: PlatformFile? = null
) : ProfilePictureInputDataRepository {
	var lastFile: PlatformFile? = null

	override suspend fun normalizeInput(file: PlatformFile): PlatformFile {
		lastFile = file
		return normalizedFile ?: file
	}
}
