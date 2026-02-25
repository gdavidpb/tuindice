package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class MainViewModelTest {
	@Test
	fun initialStartUpAction_loadsContentAndAuxiliaryActionsEmitExpectedEffects() = runBlocking {
		val settingsRepository = MainViewModelFakeSettingsRepository(
			lastDestination = LoginDestination.SignOutDialog,
			isReviewSuggested = true
		)
		val configGateway = MainViewModelFakeConfigGateway()
		val viewModel = MainViewModel(
			updateStateActionProcessor = UpdateStateActionProcessor(),
			startUpActionProcessor = StartUpActionProcessor(
				startUpUseCase = StartUpUseCase(
					sessionRepository = MainViewModelFakeSessionRepository(hasActiveSession = true),
					settingsRepository = settingsRepository,
					configRepository = configGateway,
					exceptionHandler = StartUpExceptionHandler(
						applicationRepository = MainViewModelFakeApplicationRepository(),
						reportingRepository = MainViewModelFakeReportingGateway()
					)
				)
			),
			requestReviewActionProcessor = RequestReviewActionProcessor(
				requestReviewUseCase = RequestReviewUseCase(
					settingsRepository = settingsRepository,
					configRepository = configGateway
				)
			),
			requestUpdateActionProcessor = RequestUpdateActionProcessor(
				getUpdateInfoUseCase = GetUpdateInfoUseCase(
					configRepository = configGateway,
					updateGateway = MainViewModelFakeUpdateGateway(
						updateAction = UpdateAction.Immediate
					)
				)
			),
			setLastDestinationActionProcessor = SetLastDestinationActionProcessor(
				setLastDestinationUseCase = SetLastDestinationUseCase(settingsRepository)
			)
		)
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			waitUntil("action has subscribers") { viewModel.action.subscriptionCount.value > 0 }
			viewModel.startUpAction()

			waitUntil("state reached content") { viewModel.state.value is Main.State.Content }
			val content = assertIs<Main.State.Content>(viewModel.state.value)
			assertEquals(LoginDestination.SignOutDialog, content.startDestination)

			viewModel.requestReviewAction()
			waitUntil("state remains content after review action") {
				viewModel.state.value is Main.State.Content
			}

			viewModel.checkUpdateAction()
			waitUntil("state remains content after update check action") {
				viewModel.state.value is Main.State.Content
			}

			viewModel.setLastDestinationAction(LoginDestination.SignIn)
			waitUntil("last destination persisted") {
				settingsRepository.savedDestination == LoginDestination.SignIn
			}

			viewModel.updateStateAction(Main.State.Failed)
			waitUntil("state updated to failed") { viewModel.state.value == Main.State.Failed }
		} finally {
			stateJob.cancel()
		}
	}

	private suspend fun waitUntil(
		label: String,
		timeoutMs: Long = 5_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout: $label")
	}
}

private class MainViewModelFakeSessionRepository(
	private val hasActiveSession: Boolean
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean = hasActiveSession
	override suspend fun setUsbId(usbId: String) = Unit
	override suspend fun setAccessToken(accessToken: String) = Unit
	override suspend fun setRefreshToken(refreshToken: String) = Unit
	override suspend fun getUsbId(): String = "20320000"
	override suspend fun getAccessToken(): String = "access"
	override suspend fun getRefreshToken(): String = "refresh"
	override suspend fun clear() = Unit
}

private class MainViewModelFakeSettingsRepository(
	private val lastDestination: Destination,
	private val isReviewSuggested: Boolean
) : SettingsRepository {
	var savedDestination: Destination? = null

	override suspend fun isReviewSuggested(value: Int): Boolean = isReviewSuggested
	override suspend fun getLastDestination(): Destination = lastDestination
	override suspend fun setLastDestination(destination: Destination) {
		savedDestination = destination
	}
	override suspend fun clear() = Unit
}

private class MainViewModelFakeConfigGateway : ConfigRepository {
	override suspend fun tryFetch() = Unit
	override fun getTimeout(): Long = 30_000L
	override fun getContactEmail(): String = "support@tuindice.app"
	override fun getContactSubject(): String = "Support"
	override fun getLoadingMessages(): List<String> = listOf("Cargando")
	override fun getTimeUpdateStalenessDays(): Int = 7
	override fun getSyncsToSuggestReview(): Int = 3
}

private class MainViewModelFakeUpdateGateway(
	private val updateAction: UpdateAction?
) : UpdateRepository {
	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = updateAction
	override suspend fun launchUpdate(action: UpdateAction) = Unit
}

private class MainViewModelFakeApplicationRepository : ApplicationRepository {
	override suspend fun clearData() = Unit
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}
	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true
}

private class MainViewModelFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
