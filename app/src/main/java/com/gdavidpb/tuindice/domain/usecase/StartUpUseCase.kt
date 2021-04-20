package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.utils.extensions.causes
import com.gdavidpb.tuindice.utils.extensions.isConnection
import com.gdavidpb.tuindice.utils.extensions.isInvalidLink
import com.gdavidpb.tuindice.utils.extensions.oobCode

class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val reportingRepository: ReportingRepository,
        private val linkRepository: LinkRepository,
        private val networkRepository: NetworkRepository
) : ResultUseCase<String, StartUpAction, StartUpError>() {
    override suspend fun executeOnBackground(params: String): StartUpAction {
        val isActiveAuth = authRepository.isActiveAuth()
        val isPasswordResetLink = authRepository.isResetPasswordLink(params)
        val hasCredentials = settingsRepository.hasCredentials()

        return when {
            isPasswordResetLink && hasCredentials -> handlePasswordResetLink(link = params)
            isActiveAuth -> handleSignedIn()
            hasCredentials -> handlePasswordReset()
            else -> StartUpAction.SignIn
        }
    }

    override suspend fun executeOnException(throwable: Throwable): StartUpError? {
        val causes = throwable.causes()

        return when {
            causes.isInvalidLink() -> StartUpError.InvalidLink
            throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
            else -> null
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
        val hasCache = databaseRepository.hasCache(uid = activeAuth.uid)
        val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

        check(hasCache) { "handleSignedIn no cache" }

        reportingRepository.setIdentifier(activeAuth.uid)

        return StartUpAction.Main(screen = lastScreen, account = activeAccount)
    }

    private fun handlePasswordReset(): StartUpAction {
        val email = settingsRepository.getEmail()

        return StartUpAction.ResetPassword(email = email)
    }
}