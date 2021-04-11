package com.gdavidpb.tuindice.data.source.firebase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.exception.UnauthenticatedException
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.extensions.mode
import com.gdavidpb.tuindice.utils.mappers.toAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

open class FirebaseDataSource(
        private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun isActiveAuth(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getActiveAuth(): Auth {
        return auth.currentUser
                ?.toAuth()
                ?: throw UnauthenticatedException()
    }

    override suspend fun signUp(email: String, password: String): Auth {
        return auth.createUserWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuth()
                ?: throw UnauthenticatedException()
    }

    override suspend fun signIn(email: String, password: String): Auth {
        return auth.signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuth()
                ?: throw UnauthenticatedException()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        val continueUrl = BuildConfig.URL_SIGN.format("test")

        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    override suspend fun sendVerificationEmail() {
        val continueUrl = BuildConfig.URL_SIGN.format("test")

        val currentUser = auth.currentUser ?: throw UnauthenticatedException()

        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        currentUser.sendEmailVerification(actionCodeSettings).await()
    }

    override suspend fun confirmVerifyEmail(code: String) {
        auth.applyActionCode(code).await()
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
        auth.confirmPasswordReset(code, password).await()
    }

    override suspend fun isEmailVerified(): Boolean {
        val outdatedUser = auth.currentUser ?: throw UnauthenticatedException()

        outdatedUser.reload().await()

        val updatedUser = auth.currentUser ?: throw UnauthenticatedException()

        return updatedUser.isEmailVerified
    }

    override suspend fun isVerifyEmailLink(link: String): Boolean {
        return link.mode == "verifyEmail"
    }

    override suspend fun isResetPasswordLink(link: String): Boolean {
        return link.mode == "resetPassword"
    }
}