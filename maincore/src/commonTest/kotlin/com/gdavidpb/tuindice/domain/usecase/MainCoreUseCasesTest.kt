package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class MainCoreUseCasesTest {
	@Test
	fun startUpUseCase_whenSessionIsActive_usesLastDestination() = runBlocking {
		val sessionRepository = FakeSessionRepository(hasActiveSession = true)
		val settingsRepository = FakeSettingsRepository(
			lastDestination = LoginDestination.SignOutDialog
		)
		val configGateway = FakeConfigGateway()
		val useCase = StartUpUseCase(
			sessionRepository = sessionRepository,
			settingsRepository = settingsRepository,
			configRepository = configGateway,
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = FakeApplicationRepository(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[1])
		assertEquals(LoginDestination.SignOutDialog, success.value.startDestination)
		waitUntil { configGateway.tryFetchCalls > 0 }
		assertEquals(1, configGateway.tryFetchCalls)
	}

	@Test
	fun startUpUseCase_whenSessionIsMissing_usesLoginGraph() = runBlocking {
		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(hasActiveSession = false),
			settingsRepository = FakeSettingsRepository(
				lastDestination = LoginDestination.SignOutDialog
			),
			configRepository = FakeConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = FakeApplicationRepository(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[1])
		assertEquals(LoginDestination.NavGraph, success.value.startDestination)
	}

	@Test
	fun startUpUseCase_whenConfigFetchFails_stillReturnsStartDestination() = runBlocking {
		val configGateway = FakeConfigGateway(
			tryFetchThrowable = IllegalStateException("remote config down")
		)
		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(hasActiveSession = true),
			settingsRepository = FakeSettingsRepository(
				lastDestination = LoginDestination.UpdatePasswordDialog
			),
			configRepository = configGateway,
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = FakeApplicationRepository(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[1])
		assertEquals(LoginDestination.UpdatePasswordDialog, success.value.startDestination)
		waitUntil { configGateway.tryFetchCalls > 0 }
		assertEquals(1, configGateway.tryFetchCalls)
	}

	@Test
	fun startUpUseCase_whenGooglePlayServicesFail_mapsNoServicesError() = runBlocking {
		val applicationRepository = FakeApplicationRepository()
		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(
				hasActiveSessionThrowable = GooglePlayServicesNotAvailableException()
			),
			settingsRepository = FakeSettingsRepository(
				lastDestination = LoginDestination.SignIn
			),
			configRepository = FakeConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = applicationRepository,
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[1])
		assertEquals(StartUpUseCaseError.NoServices, failure.error)
		assertEquals(0, applicationRepository.clearDataCalls)
	}

	@Test
	fun startUpUseCase_whenUnexpectedFailure_reportsUnhandledAndClearsData() = runBlocking {
		val applicationRepository = FakeApplicationRepository()
		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(
				hasActiveSessionThrowable = IllegalStateException("boom")
			),
			settingsRepository = FakeSettingsRepository(
				lastDestination = LoginDestination.SignIn
			),
			configRepository = FakeConfigGateway(),
			exceptionHandler = StartUpExceptionHandler(
				applicationRepository = applicationRepository,
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<com.gdavidpb.tuindice.domain.usecase.result.StartUpResult, StartUpUseCaseError>>(states[1])
		assertNull(failure.error)
		waitUntil { applicationRepository.clearDataCalls > 0 }
		assertEquals(1, applicationRepository.clearDataCalls)
	}

	@Test
	fun getUpdateInfoUseCase_whenUpdateExists_emitsAction() = runBlocking {
		val configGateway = FakeConfigGateway(stalenessDays = 4)
		val updateGateway = FakeUpdateGateway(updateAction = UpdateAction.Immediate)
		val useCase = GetUpdateInfoUseCase(
			configRepository = configGateway,
			updateGateway = updateGateway
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<UpdateAction, Nothing>>(states[0])
		val success = assertIs<UseCaseState.Data<UpdateAction, Nothing>>(states[1])
		assertEquals(UpdateAction.Immediate, success.value)
		assertEquals(4, updateGateway.lastStalenessDays)
	}

	@Test
	fun getUpdateInfoUseCase_whenNoUpdateAvailable_emitsOnlyLoading() = runBlocking {
		val useCase = GetUpdateInfoUseCase(
			configRepository = FakeConfigGateway(stalenessDays = 7),
			updateGateway = FakeUpdateGateway(updateAction = null)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(1, states.size)
		assertIs<UseCaseState.Loading<UpdateAction, Nothing>>(states[0])
		Unit
	}

	@Test
	fun requestReviewUseCase_whenReviewShouldBeSuggested_emitsUnit() = runBlocking {
		val settingsRepository = FakeSettingsRepository(
			isReviewSuggested = true,
			lastDestination = LoginDestination.SignIn
		)
		val configGateway = FakeConfigGateway(syncsToSuggestReview = 3)
		val useCase = RequestReviewUseCase(
			settingsRepository = settingsRepository,
			configRepository = configGateway
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
		assertIs<UseCaseState.Data<Unit, Nothing>>(states[1])
		assertEquals(3, settingsRepository.lastReviewThreshold)
	}

	@Test
	fun requestReviewUseCase_whenReviewShouldNotBeSuggested_emitsOnlyLoading() = runBlocking {
		val settingsRepository = FakeSettingsRepository(
			isReviewSuggested = false,
			lastDestination = LoginDestination.SignIn
		)
		val useCase = RequestReviewUseCase(
			settingsRepository = settingsRepository,
			configRepository = FakeConfigGateway(syncsToSuggestReview = 10)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(1, states.size)
		assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
		assertEquals(10, settingsRepository.lastReviewThreshold)
	}

	@Test
	fun setLastDestinationUseCase_savesDestination() = runBlocking {
		val settingsRepository = FakeSettingsRepository(
			lastDestination = LoginDestination.NavGraph
		)
		val useCase = SetLastDestinationUseCase(settingsRepository)

		val states = useCase.execute(LoginDestination.UpdatePasswordDialog).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, Nothing>>(states[0])
		assertIs<UseCaseState.Data<Unit, Nothing>>(states[1])
		assertEquals(LoginDestination.UpdatePasswordDialog, settingsRepository.savedDestination)
	}

	@Test
	fun startUpActionProcessor_whenSuccessful_updatesStateWithDestination() = runBlocking {
		val processor = StartUpActionProcessor(
			startUpUseCase = StartUpUseCase(
				sessionRepository = FakeSessionRepository(hasActiveSession = true),
				settingsRepository = FakeSettingsRepository(
					lastDestination = LoginDestination.UpdatePasswordDialog
				),
				configRepository = FakeConfigGateway(),
				exceptionHandler = StartUpExceptionHandler(
					applicationRepository = FakeApplicationRepository(),
					reportingRepository = FakeReportingGateway()
				)
			)
		)
		val effects = mutableListOf<Main.Effect>()
		val mutations = processor.process(
			action = Main.Action.StartUp,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = Main.State.Failed,
			mutations = mutations
		)

		assertTrue(effects.isEmpty())
		val content = assertIs<Main.State.Content>(finalState)
		assertEquals(LoginDestination.UpdatePasswordDialog, content.startDestination)
	}

	@Test
	fun startUpActionProcessor_whenNoServices_emitsDialogEffectAndFailedState() = runBlocking {
		val processor = StartUpActionProcessor(
			startUpUseCase = StartUpUseCase(
				sessionRepository = FakeSessionRepository(
					hasActiveSessionThrowable = GooglePlayServicesNotAvailableException()
				),
				settingsRepository = FakeSettingsRepository(
					lastDestination = LoginDestination.NavGraph
				),
				configRepository = FakeConfigGateway(),
				exceptionHandler = StartUpExceptionHandler(
					applicationRepository = FakeApplicationRepository(),
					reportingRepository = FakeReportingGateway()
				)
			)
		)
		val effects = mutableListOf<Main.Effect>()
		val mutations = processor.process(
			action = Main.Action.StartUp,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(
			initialState = Main.State.Starting,
			mutations = mutations
		)

		assertEquals(1, effects.size)
		assertEquals(Main.Effect.NavigateToGooglePlayServicesUnavailableDialog, effects.single())
		assertEquals(Main.State.Failed, finalState)
	}

	@Test
	fun requestReviewActionProcessor_whenReviewIsSuggested_triggersReviewEffect() = runBlocking {
		val processor = RequestReviewActionProcessor(
			requestReviewUseCase = RequestReviewUseCase(
				settingsRepository = FakeSettingsRepository(
					isReviewSuggested = true,
					lastDestination = LoginDestination.NavGraph
				),
				configRepository = FakeConfigGateway(syncsToSuggestReview = 2)
			)
		)
		val initialState = Main.State.Content(startDestination = LoginDestination.NavGraph)
		val effects = mutableListOf<Main.Effect>()
		val mutations = processor.process(
			action = Main.Action.RequestReview,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(1, effects.size)
		assertEquals(Main.Effect.TriggerReviewFlow, effects.single())
		assertEquals(initialState, finalState)
	}

	@Test
	fun requestUpdateActionProcessor_whenUpdateExists_triggersUpdateEffect() = runBlocking {
		val processor = RequestUpdateActionProcessor(
			getUpdateInfoUseCase = GetUpdateInfoUseCase(
				configRepository = FakeConfigGateway(stalenessDays = 1),
				updateGateway = FakeUpdateGateway(updateAction = UpdateAction.Immediate)
			)
		)
		val initialState = Main.State.Content(startDestination = LoginDestination.NavGraph)
		val effects = mutableListOf<Main.Effect>()
		val mutations = processor.process(
			action = Main.Action.RequestUpdateCheck,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(1, effects.size)
		val updateEffect = assertIs<Main.Effect.TriggerUpdateFlow>(effects.single())
		assertEquals(UpdateAction.Immediate, updateEffect.action)
		assertEquals(initialState, finalState)
	}

	@Test
	fun setLastDestinationActionProcessor_keepsStateAndPersistsDestination() = runBlocking {
		val settingsRepository = FakeSettingsRepository(
			lastDestination = LoginDestination.NavGraph
		)
		val processor = SetLastDestinationActionProcessor(
			setLastDestinationUseCase = SetLastDestinationUseCase(settingsRepository)
		)
		val initialState = Main.State.Content(startDestination = LoginDestination.NavGraph)
		val effects = mutableListOf<Main.Effect>()
		val mutations = processor.process(
			action = Main.Action.SetLastDestination(LoginDestination.SignOutDialog),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertTrue(effects.isEmpty())
		assertEquals(initialState, finalState)
		assertEquals(LoginDestination.SignOutDialog, settingsRepository.savedDestination)
	}

	private fun applyMutations(
		initialState: Main.State,
		mutations: List<(Main.State) -> Main.State>
	): Main.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}
	}
}

private class FakeSessionRepository(
	private val hasActiveSession: Boolean = false,
	private val hasActiveSessionThrowable: Throwable? = null
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean {
		hasActiveSessionThrowable?.let { throw it }
		return hasActiveSession
	}

	override suspend fun setUsbId(usbId: String) = Unit

	override suspend fun setAccessToken(accessToken: String) = Unit

	override suspend fun setRefreshToken(refreshToken: String) = Unit

	override suspend fun getUsbId(): String = ""

	override suspend fun getAccessToken(): String = ""

	override suspend fun getRefreshToken(): String = ""

	override suspend fun clear() = Unit
}

private class FakeSettingsRepository(
	private val isReviewSuggested: Boolean = false,
	private val lastDestination: Destination
) : SettingsRepository {
	var savedDestination: Destination? = null
	var lastReviewThreshold: Int? = null

	override suspend fun isReviewSuggested(value: Int): Boolean {
		lastReviewThreshold = value
		return isReviewSuggested
	}

	override suspend fun getLastDestination(): Destination = lastDestination

	override suspend fun setLastDestination(destination: Destination) {
		savedDestination = destination
	}

	override suspend fun clear() = Unit
}

private class FakeConfigGateway(
	private val stalenessDays: Int = 7,
	private val syncsToSuggestReview: Int = 5,
	private val tryFetchThrowable: Throwable? = null
) : ConfigGateway {
	var tryFetchCalls: Int = 0

	override suspend fun tryFetch() {
		tryFetchCalls++
		tryFetchThrowable?.let { throw it }
	}

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support"

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = stalenessDays

	override fun getSyncsToSuggestReview(): Int = syncsToSuggestReview
}

private class FakeUpdateGateway(
	private val updateAction: UpdateAction?
) : UpdateGateway {
	var lastStalenessDays: Int? = null

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		lastStalenessDays = stalenessDays
		return updateAction
	}

	override suspend fun launchUpdate(action: UpdateAction) = Unit
}

private class FakeApplicationRepository : ApplicationRepository {
	var clearDataCalls: Int = 0

	override suspend fun clearData() {
		clearDataCalls++
	}

	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true
}

private class FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private class GooglePlayServicesNotAvailableException : IllegalStateException()
