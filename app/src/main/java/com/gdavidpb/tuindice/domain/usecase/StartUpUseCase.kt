package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.utils.extensions.*

open class StartUpUseCase(
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val messagingRepository: MessagingRepository,
        private val reportingRepository: ReportingRepository,
        private val linkRepository: LinkRepository,
        private val configRepository: ConfigRepository,
        private val networkRepository: NetworkRepository
) : ResultUseCase<String, StartUpAction, StartUpError>() {
    override suspend fun executeOnBackground(params: String): StartUpAction? {
        val isActiveAuth = authRepository.isActiveAuth()
        val isPasswordResetLink = authRepository.isResetPasswordLink(params)
        val isVerifyEmailLink = authRepository.isVerifyEmailLink(params)
        val hasCredentials = settingsRepository.hasCredentials()

        val lastScreen = settingsRepository.getLastScreen()

        configRepository.tryFetchAndActivate()

        return when {
            isVerifyEmailLink && isActiveAuth -> {
                val oobCode = linkRepository.resolveLink(data = params).oobCode

                val isEmailVerified = authRepository.isEmailVerified()

                if (!isEmailVerified) authRepository.confirmVerifyEmail(code = oobCode)

                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

                settingsRepository.resetCountdown()

                StartUpAction.Main(screen = lastScreen, account = activeAccount)
            }
            isPasswordResetLink && hasCredentials -> {
                val oobCode = linkRepository.resolveLink(data = params).oobCode
                val credentials = settingsRepository.getCredentials()

                authRepository.confirmPasswordReset(code = oobCode, password = credentials.password)

                val activeAuth = authRepository.signIn(credentials = credentials)
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

                settingsRepository.resetCountdown()

                StartUpAction.Main(screen = lastScreen, account = activeAccount)
            }
            isActiveAuth -> {
                val activeAuth = authRepository.getActiveAuth()
                val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)
                val isEmailVerified = authRepository.isEmailVerified()

                reportingRepository.setIdentifier(activeAuth.uid)

                val token = messagingRepository.getToken()

                if (token != null)
                    databaseRepository.setToken(uid = activeAuth.uid, token = token)

                if (isEmailVerified)
                    StartUpAction.Main(screen = lastScreen, account = activeAccount)
                else
                    StartUpAction.Verify(email = activeAuth.email)
            }
            hasCredentials -> {
                val email = settingsRepository.getEmail()

                StartUpAction.Reset(email = email)
            }
            else -> StartUpAction.SignIn
        }
    }

    override suspend fun executeOnException(throwable: Throwable): StartUpError? {
        val causes = throwable.causes()

        return when {
            causes.isInvalidLink() -> StartUpError.InvalidLink
            causes.isAccountDisabled() -> StartUpError.AccountDisabled
            throwable.isConnectionIssue() -> StartUpError.NoConnection(networkRepository.isAvailable())
            else -> StartUpError.UnableToStart
        }
    }
}