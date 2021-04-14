package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.utils.extensions.*

class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val messagingRepository: MessagingRepository,
        private val reportingRepository: ReportingRepository,
        private val linkRepository: LinkRepository,
        private val configRepository: ConfigRepository,
        private val networkRepository: NetworkRepository
) : ResultUseCase<String, StartUpAction, StartUpError>() {
    override suspend fun executeOnBackground(params: String): StartUpAction {
        val isActiveAuth = authRepository.isActiveAuth()
        val isPasswordResetLink = authRepository.isResetPasswordLink(params)
        val hasCredentials = settingsRepository.hasCredentials()

        configRepository.tryFetchAndActivate()

        return when {
            isPasswordResetLink && hasCredentials -> handlePasswordResetLink(link = params)
            isActiveAuth -> handleSignedIn()
            hasCredentials -> handlePasswordReset()
            else -> StartUpAction.SignIn
        }
    }

    override suspend fun executeOnException(throwable: Throwable): StartUpError {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> StartUpError.AccountDisabled
            causes.isInvalidLink() -> StartUpError.InvalidLink
            throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
            else -> StartUpError.UnableToStart
        }
    }

    private suspend fun handlePasswordResetLink(link: String): StartUpAction {
        val lastScreen = settingsRepository.getLastScreen()
        val oobCode = linkRepository.resolveLink(data = link).oobCode
        val credentials = settingsRepository.getCredentials()

        authRepository.confirmPasswordReset(code = oobCode, password = credentials.password)

        val activeAuth = authRepository.signIn(credentials = credentials)
        val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

        settingsRepository.resetCountdown()

        return StartUpAction.Main(screen = lastScreen, account = activeAccount)
    }

    private suspend fun handleSignedIn(): StartUpAction {
        val lastScreen = settingsRepository.getLastScreen()
        val activeAuth = authRepository.getActiveAuth()
        val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

        reportingRepository.setIdentifier(activeAuth.uid)

        val token = messagingRepository.getToken()

        if (token != null) databaseRepository.setToken(uid = activeAuth.uid, token = token)

        return StartUpAction.Main(screen = lastScreen, account = activeAccount)
    }

    private fun handlePasswordReset(): StartUpAction {
        val email = settingsRepository.getEmail()

        return StartUpAction.ResetPassword(email = email)
    }
}