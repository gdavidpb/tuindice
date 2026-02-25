package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.FileGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SummaryActionProcessorsExtendedTest {
	@Test
	fun loadSummaryActionProcessor_whenData_mapsToContent() = runBlocking {
		val processor = LoadSummaryActionProcessor(
			getUserUseCase = GetUserUseCase(
				userRepository = FakeSummaryUserRepository(),
				exceptionHandler = GetUserExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			),
			textProvider = FakeSummaryTextProvider
		)

		val mutations = processor.process(
			action = Summary.Action.LoadSummary,
			sideEffect = {}
		).toList()
		assertIs<UseCaseState.Loading<User, GetUserUseCaseError>>(
			GetUserUseCase(
				userRepository = FakeSummaryUserRepository(),
				exceptionHandler = GetUserExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			).execute(Unit).toList().first()
		)

		val finalState = applyMutations(
			initialState = Summary.State.Loading,
			mutations = mutations
		)

		val content = assertIs<Summary.State.Content>(finalState)
		assertEquals("Ada Lovelace", content.name)
		assertEquals("updated-1700000000000", content.lastUpdate)
		assertEquals("https://tuindice.app/profile.jpg", content.profilePictureUrl)
		assertTrue(content.isUpdated)
	}

	@Test
	fun takeProfilePictureActionProcessor_emitsOpenCameraEffect() = runBlocking {
		val processor = TakeProfilePictureActionProcessor(
			takeProfilePictureUseCase = TakeProfilePictureUseCase(
				applicationRepository = FakeFileGateway()
			),
			textProvider = FakeSummaryTextProvider
		)
		val effects = mutableListOf<Summary.Effect>()
		val initialState = contentState(profilePictureUrl = "https://tuindice.app/profile.jpg")

		val mutations = processor.process(
			action = Summary.Action.TakeProfilePicture,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(initialState, finalState)
		val openCamera = assertIs<Summary.Effect.OpenCamera>(effects.single())
		assertEquals(PlatformFileRef("/tmp/profile_picture.jpg"), openCamera.output)
	}

	@Test
	fun uploadProfilePictureActionProcessor_whenSuccess_updatesStateAndShowsSnackBar() = runBlocking {
		val userRepository = FakeSummaryUserRepository()
		val encoderRepository = FakeSummaryEncoderRepository()
		val processor = UploadProfilePictureActionProcessor(
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				encoderRepository = encoderRepository,
				paramsValidator = UploadProfilePictureParamsValidator(),
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			),
			textProvider = FakeSummaryTextProvider
		)
		val effects = mutableListOf<Summary.Effect>()
		val initialState = contentState(profilePictureUrl = "https://tuindice.app/old.jpg")

		val mutations = processor.process(
			action = Summary.Action.UploadProfilePicture(PlatformUri("content://new-profile")),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val content = assertIs<Summary.State.Content>(finalState)
		assertEquals("https://tuindice.app/new-profile.jpg", content.profilePictureUrl)
		assertEquals(false, content.isProfilePictureLoading)
		assertEquals(PlatformUri("content://new-profile"), encoderRepository.lastUri)
		val snackBar = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals("Profile picture updated", snackBar.message)
	}

	@Test
	fun uploadProfilePictureActionProcessor_whenInvalidSource_showsDefaultErrorAndKeepsPicture() = runBlocking {
		val processor = UploadProfilePictureActionProcessor(
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = FakeSummaryUserRepository(),
				encoderRepository = FakeSummaryEncoderRepository(),
				paramsValidator = UploadProfilePictureParamsValidator(),
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			),
			textProvider = FakeSummaryTextProvider
		)
		val effects = mutableListOf<Summary.Effect>()
		val initialState = contentState(profilePictureUrl = "https://tuindice.app/old.jpg")

		val mutations = processor.process(
			action = Summary.Action.UploadProfilePicture(PlatformUri("")),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val content = assertIs<Summary.State.Content>(finalState)
		assertEquals("https://tuindice.app/old.jpg", content.profilePictureUrl)
		assertEquals(false, content.isProfilePictureLoading)
		val snackBar = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	@Test
	fun confirmRemoveProfilePictureActionProcessor_whenSuccess_clearsPictureAndShowsSnackBar() = runBlocking {
		val userRepository = FakeSummaryUserRepository()
		val processor = ConfirmRemoveProfilePictureActionProcessor(
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				exceptionHandler = RemoveProfilePictureExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			),
			textProvider = FakeSummaryTextProvider
		)
		val effects = mutableListOf<Summary.Effect>()
		val initialState = contentState(profilePictureUrl = "https://tuindice.app/profile.jpg")

		val mutations = processor.process(
			action = Summary.Action.ConfirmRemoveProfilePicture,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val content = assertIs<Summary.State.Content>(finalState)
		assertEquals("", content.profilePictureUrl)
		assertEquals(false, content.isProfilePictureLoading)
		assertEquals(1, userRepository.removeCalls)
		val snackBar = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals("Profile picture removed", snackBar.message)
	}

	@Test
	fun confirmRemoveProfilePictureActionProcessor_whenTimeout_showsTimeoutAndKeepsPicture() = runBlocking {
		val userRepository = FakeSummaryUserRepository(
			removeThrowable = timeoutThrowable()
		)
		val processor = ConfirmRemoveProfilePictureActionProcessor(
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				exceptionHandler = RemoveProfilePictureExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway
				)
			),
			textProvider = FakeSummaryTextProvider
		)
		val effects = mutableListOf<Summary.Effect>()
		val initialState = contentState(profilePictureUrl = "https://tuindice.app/profile.jpg")

		val mutations = processor.process(
			action = Summary.Action.ConfirmRemoveProfilePicture,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		val content = assertIs<Summary.State.Content>(finalState)
		assertEquals("https://tuindice.app/profile.jpg", content.profilePictureUrl)
		assertEquals(false, content.isProfilePictureLoading)
		val snackBar = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals("Timeout", snackBar.message)
	}

	private fun contentState(profilePictureUrl: String): Summary.State.Content {
		return Summary.State.Content(
			name = "Ada Lovelace",
			lastUpdate = "updated-1700000000000",
			careerName = "Computer Science",
			grade = 3.9f,
			enrolledSubjects = 5,
			enrolledCredits = 20,
			approvedSubjects = 20,
			approvedCredits = 80,
			retiredSubjects = 1,
			retiredCredits = 4,
			failedSubjects = 0,
			failedCredits = 0,
			profilePictureUrl = profilePictureUrl,
			isGradeVisible = true,
			isProfilePictureLoading = false,
			isLoading = false,
			isUpdated = true,
			isUpdating = false
		)
	}

	private fun applyMutations(
		initialState: Summary.State,
		mutations: List<(Summary.State) -> Summary.State>
	): Summary.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object FakeSummaryTextProvider : SummaryTextProvider {
	override fun lastUpdate(lastUpdate: Long): String = "updated-$lastUpdate"
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun timeout(): String = "Timeout"
	override fun noService(): String = "No service"
	override fun defaultError(): String = "Default error"
	override fun profilePictureUpdated(): String = "Profile picture updated"
	override fun profilePictureRemoved(): String = "Profile picture removed"
}

private class FakeSummaryUserRepository(
	private val uploadThrowable: Throwable? = null,
	private val removeThrowable: Throwable? = null
) : UserRepository {
	var removeCalls: Int = 0

	override suspend fun getUserFlow(): Flow<User> {
		return flowOf(
			User(
				id = "user-1",
				cid = "V12345",
				usbId = "20-32000",
				email = "ada@tuindice.app",
				pictureUrl = "https://tuindice.app/profile.jpg",
				fullName = "Ada Lovelace",
				firstNames = "Ada Augusta",
				lastNames = "Lovelace Byron",
				careerName = "Computer Science",
				careerCode = 10,
				scholarship = false,
				grade = 3.84,
				enrolledSubjects = 5,
				enrolledCredits = 20,
				approvedSubjects = 20,
				approvedCredits = 80,
				retiredSubjects = 1,
				retiredCredits = 4,
				failedSubjects = 0,
				failedCredits = 0,
				lastUpdate = 1_700_000_000_000L
			)
		)
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		uploadThrowable?.let { throw it }
		return ProfilePicture("https://tuindice.app/new-profile.jpg")
	}

	override suspend fun removeProfilePicture() {
		removeThrowable?.let { throw it }
		removeCalls++
	}
}

private class FakeSummaryEncoderRepository : EncoderRepository {
	var lastUri: PlatformUri? = null

	override suspend fun encodePicture(uri: PlatformUri): EncodedImage {
		lastUri = uri
		return EncodedImage(
			content = byteArrayOf(1, 2, 3),
			mimeType = "image/jpeg"
		)
	}
}

private class FakeFileGateway : FileGateway {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/profile_picture.jpg")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true
}

private class FakeNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private object FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun timeoutThrowable(): Throwable = runBlocking {
	val throwable = runCatching {
		withTimeout(1) {
			delay(5)
		}
	}.exceptionOrNull()

	checkNotNull(throwable)
}
