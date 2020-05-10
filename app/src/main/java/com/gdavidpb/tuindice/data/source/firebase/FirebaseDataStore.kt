package com.gdavidpb.tuindice.data.source.firebase

import android.net.Uri
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.extensions.await
import com.gdavidpb.tuindice.utils.mappers.fromResetParam
import com.gdavidpb.tuindice.utils.mappers.toAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

open class FirebaseDataStore(
        private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun isActiveAuth(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun getActiveAuth(): Auth {
        return auth.currentUser
                ?.toAuth()
                ?: throw NoAuthenticatedException()
    }

    override suspend fun signUp(email: String, password: String): Auth {
        return auth.createUserWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuth()
                ?: throw NoAuthenticatedException()
    }

    override suspend fun signIn(email: String, password: String): Auth {
        return auth.signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuth()
                ?: throw NoAuthenticatedException()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String, password: String) {
        val resetPassword = (email to password).fromResetParam()
        val continueUrl = BuildConfig.LINK_RESET_PASSWORD.format(resetPassword)

        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    override suspend fun sendVerificationEmail() {
        val currentUser = auth.currentUser ?: throw NoAuthenticatedException()
        val continueUrl = BuildConfig.LINK_VERIFY.format(currentUser.uid)

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
        val outdatedUser = auth.currentUser ?: throw NoAuthenticatedException()

        outdatedUser.reload().await()

        val updatedUser = auth.currentUser ?: throw NoAuthenticatedException()

        return updatedUser.isEmailVerified
    }

    override suspend fun isVeryLink(link: String): Boolean {
        if (link.isEmpty()) return false

        return Uri.parse(link).getQueryParameter("mode") == "verifyEmail"
    }

    override suspend fun isResetLink(link: String): Boolean {
        if (link.isEmpty()) return false

        return Uri.parse(link).getQueryParameter("mode") == "resetPassword"
    }
}