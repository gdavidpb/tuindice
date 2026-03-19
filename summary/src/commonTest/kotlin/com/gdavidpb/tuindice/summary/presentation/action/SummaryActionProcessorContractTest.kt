package com.gdavidpb.tuindice.summary.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
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
	fun observeSummaryActionProcessor_reducesStateToContent() = runTest {
		val processor = createObserveSummaryActionProcessor(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.ObserveSummary,
			sideEffect = effects::add
		).test {
			val content = assertIs<Summary.State.Content>(awaitItem()(Summary.State.Loading()))
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
	fun observeSummaryActionProcessor_formatsZeroLastUpdateAsNunca() = runTest {
		val processor = createObserveSummaryActionProcessor(
			userRepository = RecordingUserRepository(
				users = flowOf(DEFAULT_SUMMARY_USER.copy(lastUpdate = 0L))
			)
		)

		processor.process(
			action = Summary.Action.ObserveSummary,
			sideEffect = {}
		).test {
			val content = assertIs<Summary.State.Content>(awaitItem()(Summary.State.Loading()))
			assertEquals("Última actualización: Nunca", content.lastUpdate)

			awaitComplete()
		}
	}

	@Test
	fun refreshSummaryActionProcessor_setsLoadingFromFailedState() = runTest {
		val processor = createRefreshSummaryActionProcessor(
			userRepository = RecordingUserRepository()
		)

		processor.process(
			action = Summary.Action.RefreshSummary,
			sideEffect = {}
		).test {
			val loading = awaitItem()(Summary.State.Failed())
			assertEquals(Summary.State.Loading(isUserRefreshing = true), loading)

			val idleLoading = awaitItem()(loading)
			assertEquals(Summary.State.Loading(isUserRefreshing = false), idleLoading)

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
			isProfilePictureLoading = false,
			isUserRefreshing = false
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
			isProfilePictureLoading = false,
			isUserRefreshing = false
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
	fun confirmRemoveProfilePictureActionProcessor_treatsNotFoundAsSuccessAndStopsLoading() = runTest {
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
			isProfilePictureLoading = false,
			isUserRefreshing = false
		)
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.ConfirmRemoveProfilePicture,
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
		assertEquals(getString(Res.string.snack_profile_picture_removed), effect.message)
	}

	@Test
	fun openProfilePictureSettingsActionProcessor_ignoresActionWhileUserIsRefreshing() = runTest {
		val processor = OpenProfilePictureSettingsActionProcessor()
		val effects = mutableListOf<Summary.Effect>()

		processor.process(
			action = Summary.Action.OpenProfilePictureSettings,
			sideEffect = effects::add
		).test {
			val state = summaryStateContent(isUserRefreshing = true)

			assertEquals(state, awaitItem()(state))
			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	private fun createObserveSummaryActionProcessor(
		userRepository: UserRepository = RecordingUserRepository()
	): ObserveSummaryActionProcessor {
		return ObserveSummaryActionProcessor(
			observeUserUseCase = ObserveUserUseCase(
				userRepository = userRepository
			)
		)
	}

	private fun createRefreshSummaryActionProcessor(
		userRepository: UserRepository = RecordingUserRepository()
	): RefreshSummaryActionProcessor {
		return RefreshSummaryActionProcessor(
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				exceptionHandler = UpdateUserExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
	}

	private suspend fun summaryStateContent(
		isUserRefreshing: Boolean = false
	): Summary.State.Content {
		return Summary.State.Content(
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
			isProfilePictureLoading = false,
			isUserRefreshing = isUserRefreshing
		)
	}
}
