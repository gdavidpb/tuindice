package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProfilePictureUseCasesTest {
	@Test
	fun getUserUseCase_returnsUserFromRepositoryFlow() = runBlocking {
		val fakeUserRepository = FakeUserRepository()
		val useCase = GetUserUseCase(
			userRepository = fakeUserRepository,
			exceptionHandler = GetUserExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<User, GetUserUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<User, GetUserUseCaseError>>(states[1])
		assertEquals("u-1", success.value.id)
		assertEquals("user@tuindice.app", success.value.email)
	}

	@Test
	fun uploadProfilePicture_encodesAndUploadsImage() = runBlocking {
		val fakeUserRepository = FakeUserRepository()
		val fakeEncoderRepository = FakeEncoderRepository(
			encodedImage = EncodedImage(
				content = byteArrayOf(10, 11, 12),
				mimeType = "image/jpeg"
			)
		)
		val useCase = UploadProfilePictureUseCase(
			userRepository = fakeUserRepository,
			encoderRepository = fakeEncoderRepository,
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(PlatformUri("content://profile-picture")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<String, ProfilePictureUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<String, ProfilePictureUseCaseError>>(states[1])
		assertEquals("https://tuindice.app/picture.jpg", success.value)
		assertEquals(PlatformUri("content://profile-picture"), fakeEncoderRepository.lastUri)
		assertContentEquals(byteArrayOf(10, 11, 12), fakeUserRepository.lastUploadContent)
		assertEquals("image/jpeg", fakeUserRepository.lastUploadMimeType)
	}

	@Test
	fun uploadProfilePicture_emptyUriReportsInvalidSource() = runBlocking {
		val fakeUserRepository = FakeUserRepository()
		val fakeEncoderRepository = FakeEncoderRepository(
			encodedImage = EncodedImage(
				content = byteArrayOf(1),
				mimeType = "image/jpeg"
			)
		)
		val useCase = UploadProfilePictureUseCase(
			userRepository = fakeUserRepository,
			encoderRepository = fakeEncoderRepository,
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(PlatformUri("")).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<String, ProfilePictureUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<String, ProfilePictureUseCaseError>>(states[1])
		assertEquals(ProfilePictureUseCaseError.InvalidSource, failure.error)
		assertEquals(null, fakeUserRepository.lastUploadContent)
	}

	@Test
	fun takeProfilePicture_createsExpectedTemporaryFile() = runBlocking {
		val expectedFile = PlatformFileRef("/tmp/profile_picture.jpg")
		val useCase = TakeProfilePictureUseCase(
			applicationRepository = FakeFileGateway(expectedFile)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<PlatformFileRef, ProfilePictureUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<PlatformFileRef, ProfilePictureUseCaseError>>(states[1])
		assertEquals(expectedFile, success.value)
	}

	@Test
	fun removeProfilePicture_invokesRepositoryDelete() = runBlocking {
		val fakeUserRepository = FakeUserRepository()
		val useCase = RemoveProfilePictureUseCase(
			userRepository = fakeUserRepository,
			exceptionHandler = RemoveProfilePictureExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, ProfilePictureUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, ProfilePictureUseCaseError>>(states[1])
		assertEquals(1, fakeUserRepository.removeCalls)
	}
}

private class FakeUserRepository : UserRepository {
	var lastUploadContent: ByteArray? = null
	var lastUploadMimeType: String? = null
	var removeCalls: Int = 0

	override suspend fun getUserFlow(): Flow<User> {
		return flowOf(
			User(
				id = "u-1",
				cid = "V12345",
				usbId = "20320000",
				email = "user@tuindice.app",
				pictureUrl = "",
				fullName = "User Example",
				firstNames = "User",
				lastNames = "Example",
				careerName = "Engineering",
				careerCode = 10,
				scholarship = false,
				grade = 18.25,
				enrolledSubjects = 6,
				enrolledCredits = 18,
				approvedSubjects = 5,
				approvedCredits = 15,
				retiredSubjects = 1,
				retiredCredits = 3,
				failedSubjects = 0,
				failedCredits = 0,
				lastUpdate = 1_700_000_000_000L
			)
		)
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		lastUploadContent = content
		lastUploadMimeType = mimeType
		return ProfilePicture(url = "https://tuindice.app/picture.jpg")
	}

	override suspend fun removeProfilePicture() {
		removeCalls++
	}
}

private class FakeEncoderRepository(
	private val encodedImage: EncodedImage
) : EncoderRepository {
	var lastUri: PlatformUri? = null

	override suspend fun encodePicture(uri: PlatformUri): EncodedImage {
		lastUri = uri
		return encodedImage
	}
}

private class FakeFileGateway(
	private val temporaryFile: PlatformFileRef
) : FileRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return temporaryFile
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean {
		return true
	}
}

private class FakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
