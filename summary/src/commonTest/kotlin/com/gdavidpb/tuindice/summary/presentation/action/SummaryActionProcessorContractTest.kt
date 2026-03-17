package com.gdavidpb.tuindice.summary.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_profile_picture_not_image
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_updated
import tuindice.summary.generated.resources.text_sync_healthy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SummaryActionProcessorContractTest {
	@Test
	fun loadSummaryActionProcessor_reducesStateToContent() = runTest {
		val processor = LoadSummaryActionProcessor(
			getUserUseCase = GetUserUseCase(
				userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER)),
				exceptionHandler = GetUserExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.LoadSummary,
			sideEffect = effects::add
		).test {
			val loading = awaitItem()(Summary.State.Failed)
			assertEquals(Summary.State.Loading, loading)

			val content = assertIs<Summary.State.Content>(awaitItem()(Summary.State.Loading))
			assertEquals("Ana Diaz", content.name)
			assertEquals(
				getString(
					Res.string.text_sync_healthy,
					DEFAULT_SUMMARY_USER.lastUpdate.formatLastUpdate()
				),
				content.lastUpdate
			)
			assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, content.profilePictureUrl)

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadSummaryActionProcessor_formatsZeroLastUpdateAsNunca() = runTest {
		val processor = LoadSummaryActionProcessor(
			getUserUseCase = GetUserUseCase(
				userRepository = RecordingUserRepository(
					users = flowOf(DEFAULT_SUMMARY_USER.copy(lastUpdate = 0L))
				),
				exceptionHandler = GetUserExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)

		processor.process(
			action = Summary.Action.LoadSummary,
			sideEffect = {}
		).test {
			awaitItem()(Summary.State.Failed)

			val content = assertIs<Summary.State.Content>(awaitItem()(Summary.State.Loading))
			assertEquals("Última actualización: Nunca", content.lastUpdate)

			awaitComplete()
		}
	}

	@Test
	fun uploadProfilePictureActionProcessor_setsLoadingThenUpdatesPictureAndSnackbar() = runTest {
		val processor = UploadProfilePictureActionProcessor(
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = RecordingUserRepository(),
				paramsValidator = UploadProfilePictureParamsValidator(),
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = Summary.State.Content(
			name = "Ana Diaz",
			lastUpdate = getString(
				Res.string.text_sync_healthy,
				DEFAULT_SUMMARY_USER.lastUpdate.formatLastUpdate()
			),
			careerName = DEFAULT_SUMMARY_USER.careerName,
			grade = DEFAULT_SUMMARY_USER.grade.toFloat(),
			enrolledSubjects = DEFAULT_SUMMARY_USER.enrolledSubjects,
			enrolledCredits = DEFAULT_SUMMARY_USER.enrolledCredits,
			approvedSubjects = DEFAULT_SUMMARY_USER.approvedSubjects,
			approvedCredits = DEFAULT_SUMMARY_USER.approvedCredits,
			retiredSubjects = DEFAULT_SUMMARY_USER.retiredSubjects,
			retiredCredits = DEFAULT_SUMMARY_USER.retiredCredits,
			failedSubjects = DEFAULT_SUMMARY_USER.failedSubjects,
			failedCredits = DEFAULT_SUMMARY_USER.failedCredits,
			profilePictureUrl = DEFAULT_SUMMARY_USER.pictureUrl,
			isGradeVisible = true,
			isProfilePictureLoading = false
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.UploadProfilePicture(PlatformFile("content://profile/new.jpg")),
			sideEffect = effects::add
		).test {
			val loading = assertIs<Summary.State.Content>(awaitItem()(initialState))
			assertTrue(loading.isProfilePictureLoading)

			val content = assertIs<Summary.State.Content>(awaitItem()(loading))
			assertEquals("https://cdn.tuindice.app/profile/updated.jpg", content.profilePictureUrl)
			assertEquals(false, content.isProfilePictureLoading)

			awaitComplete()
		}

		val effect = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_profile_picture_updated), effect.message)
	}

	@Test
	fun uploadProfilePictureActionProcessor_showsSpecificMessageWhenFileIsNotImage() = runTest {
		val processor = UploadProfilePictureActionProcessor(
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = RecordingUserRepository(
					throwable = clientRequestException(
						statusCode = HttpStatusCode.UnsupportedMediaType,
						path = "/users/v1/picture"
					)
				),
				paramsValidator = UploadProfilePictureParamsValidator(),
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = Summary.State.Content(
			name = "Ana Diaz",
			lastUpdate = getString(
				Res.string.text_sync_healthy,
				DEFAULT_SUMMARY_USER.lastUpdate.formatLastUpdate()
			),
			careerName = DEFAULT_SUMMARY_USER.careerName,
			grade = DEFAULT_SUMMARY_USER.grade.toFloat(),
			enrolledSubjects = DEFAULT_SUMMARY_USER.enrolledSubjects,
			enrolledCredits = DEFAULT_SUMMARY_USER.enrolledCredits,
			approvedSubjects = DEFAULT_SUMMARY_USER.approvedSubjects,
			approvedCredits = DEFAULT_SUMMARY_USER.approvedCredits,
			retiredSubjects = DEFAULT_SUMMARY_USER.retiredSubjects,
			retiredCredits = DEFAULT_SUMMARY_USER.retiredCredits,
			failedSubjects = DEFAULT_SUMMARY_USER.failedSubjects,
			failedCredits = DEFAULT_SUMMARY_USER.failedCredits,
			profilePictureUrl = DEFAULT_SUMMARY_USER.pictureUrl,
			isGradeVisible = true,
			isProfilePictureLoading = false
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.UploadProfilePicture(PlatformFile("content://profile/new.jpg")),
			sideEffect = effects::add
		).test {
			val loading = assertIs<Summary.State.Content>(awaitItem()(initialState))
			assertTrue(loading.isProfilePictureLoading)

			val content = assertIs<Summary.State.Content>(awaitItem()(loading))
			assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, content.profilePictureUrl)
			assertEquals(false, content.isProfilePictureLoading)

			awaitComplete()
		}

		val effect = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_profile_picture_not_image), effect.message)
	}

	@Test
	fun confirmRemoveProfilePictureActionProcessor_treatsNotFoundAsSuccessAndClearsPicture() = runTest {
		val processor = ConfirmRemoveProfilePictureActionProcessor(
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = RecordingUserRepository(
					throwable = clientRequestException(
						statusCode = HttpStatusCode.NotFound,
						path = "/users/v1/picture"
					)
				),
				exceptionHandler = RemoveProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = Summary.State.Content(
			name = "Ana Diaz",
			lastUpdate = getString(
				Res.string.text_sync_healthy,
				DEFAULT_SUMMARY_USER.lastUpdate.formatLastUpdate()
			),
			careerName = DEFAULT_SUMMARY_USER.careerName,
			grade = DEFAULT_SUMMARY_USER.grade.toFloat(),
			enrolledSubjects = DEFAULT_SUMMARY_USER.enrolledSubjects,
			enrolledCredits = DEFAULT_SUMMARY_USER.enrolledCredits,
			approvedSubjects = DEFAULT_SUMMARY_USER.approvedSubjects,
			approvedCredits = DEFAULT_SUMMARY_USER.approvedCredits,
			retiredSubjects = DEFAULT_SUMMARY_USER.retiredSubjects,
			retiredCredits = DEFAULT_SUMMARY_USER.retiredCredits,
			failedSubjects = DEFAULT_SUMMARY_USER.failedSubjects,
			failedCredits = DEFAULT_SUMMARY_USER.failedCredits,
			profilePictureUrl = DEFAULT_SUMMARY_USER.pictureUrl,
			isGradeVisible = true,
			isProfilePictureLoading = false
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.ConfirmRemoveProfilePicture,
			sideEffect = effects::add
		).test {
			val loading = assertIs<Summary.State.Content>(awaitItem()(initialState))
			assertTrue(loading.isProfilePictureLoading)

			val content = assertIs<Summary.State.Content>(awaitItem()(loading))
			assertEquals("", content.profilePictureUrl)
			assertEquals(false, content.isProfilePictureLoading)

			awaitComplete()
		}

		val effect = assertIs<Summary.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_profile_picture_removed), effect.message)
	}
}
